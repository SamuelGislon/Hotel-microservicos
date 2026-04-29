package com.hosped.ms_pagamentos.service;

import com.hosped.ms_pagamentos.config.RabbitMQConfig;
import com.hosped.ms_pagamentos.dto.PagamentoEventoRetornoDTO;
import com.hosped.ms_pagamentos.dto.PagamentoResponseDTO;
import com.hosped.ms_pagamentos.dto.ReservaEventoDTO;
import com.hosped.ms_pagamentos.dto.StatusPagamentoIntegracaoDTO;
import com.hosped.ms_pagamentos.email.EmailService;
import com.hosped.ms_pagamentos.model.Pagamento;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import com.hosped.ms_pagamentos.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;

    @Value("${pagamento.expiracao.horas}")
    private int horasExpiracao;

    @Transactional
    public Pagamento processarReserva(ReservaEventoDTO evento) {
        long dias = ChronoUnit.DAYS.between(evento.getCheckIn(), evento.getCheckOut());
        BigDecimal valor = evento.getValorDiaria().multiply(BigDecimal.valueOf(dias));

        Pagamento pagamento = new Pagamento();
        pagamento.setReservaId(evento.getReservaId());
        pagamento.setValor(valor);
        pagamento.setMetodoPagamento(evento.getMetodoPagamento());
        pagamento.setNomeHospede(evento.getNomeHospede());
        pagamento.setEmailHospede(evento.getEmailHospede());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataCriacao(LocalDateTime.now());
        pagamento.setDataExpiracao(LocalDateTime.now().plusHours(horasExpiracao));

        Pagamento salvo = pagamentoRepository.save(pagamento);
        emailService.enviarEmailPagamentoPendente(salvo);
        return salvo;
    }

    @Transactional
    public void confirmarPagamento(String id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado: " + id));

        confirmarPagamentoPendente(pagamento);
    }

    @Transactional
    public void confirmarPagamentoPorReserva(String reservaId) {
        List<Pagamento> pendentes = pagamentoRepository.findByReservaIdAndStatus(reservaId, StatusPagamento.PENDENTE);
        if (!pendentes.isEmpty()) {
            confirmarPagamentoPendente(pendentes.get(0));
            return;
        }

        List<Pagamento> pagamentos = pagamentoRepository.findByReservaId(reservaId);
        if (pagamentos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado para a reserva: " + reservaId);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Pagamento não pode ser confirmado — status atual: " + pagamentos.get(0).getStatus()
        );
    }

    @Transactional
    public void atualizarStatus(String pagamentoId, StatusPagamento novoStatus) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado: " + pagamentoId));

        aplicarStatus(pagamento, novoStatus);
    }

    @Transactional
    public int expirarPagamentosVencidos() {
        List<Pagamento> vencidos = pagamentoRepository.findByStatusAndDataExpiracaoBefore(
                StatusPagamento.PENDENTE,
                LocalDateTime.now()
        );

        vencidos.forEach(pagamento -> aplicarStatus(pagamento, StatusPagamento.EXPIRADO));
        return vencidos.size();
    }

    private void confirmarPagamentoPendente(Pagamento pagamento) {
        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Pagamento não pode ser confirmado — status atual: " + pagamento.getStatus());
        }

        if (LocalDateTime.now().isAfter(pagamento.getDataExpiracao())) {
            aplicarStatus(pagamento, StatusPagamento.EXPIRADO);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Pagamento expirado — não é possível confirmar");
        }

        aplicarStatus(pagamento, StatusPagamento.APROVADO);
    }

    private void aplicarStatus(Pagamento pagamento, StatusPagamento novoStatus) {
        pagamento.setStatus(novoStatus);
        pagamento.setDataAtualizacao(LocalDateTime.now());
        pagamentoRepository.save(pagamento);

        publicarRetorno(pagamento);

        if (novoStatus == StatusPagamento.APROVADO) {
            emailService.enviarEmailPagamentoAprovado(pagamento);
        } else if (novoStatus == StatusPagamento.EXPIRADO) {
            emailService.enviarEmailPagamentoExpirado(pagamento);
        }
    }

    private void publicarRetorno(Pagamento pagamento) {
        PagamentoEventoRetornoDTO retorno = new PagamentoEventoRetornoDTO();
        retorno.setReservaId(pagamento.getReservaId());
        retorno.setStatus(pagamento.getStatus());
        retorno.setDataAtualizacao(pagamento.getDataAtualizacao());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_PAGAMENTO_PROCESSADO,
                retorno
        );
    }

    public PagamentoResponseDTO buscarPorId(String id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado: " + id));
        return toResponseDTO(pagamento);
    }

    public List<PagamentoResponseDTO> buscarPorReservaId(String reservaId) {
        return pagamentoRepository.findByReservaId(reservaId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public StatusPagamentoIntegracaoDTO consultarStatusIntegracao(String reservaId) {
        List<Pagamento> pagamentos = pagamentoRepository.findByReservaId(reservaId);
        if (pagamentos.isEmpty()) {
            return new StatusPagamentoIntegracaoDTO(
                    reservaId,
                    "SEM_PAGAMENTO",
                    false,
                    "Nenhum pagamento encontrado para a reserva"
            );
        }

        Pagamento pagamento = pagamentos.get(0);
        return new StatusPagamentoIntegracaoDTO(
                reservaId,
                pagamento.getStatus().name(),
                false,
                "Status de pagamento consultado com sucesso"
        );
    }

    private PagamentoResponseDTO toResponseDTO(Pagamento pagamento) {
        PagamentoResponseDTO dto = new PagamentoResponseDTO();
        dto.setId(pagamento.getId());
        dto.setReservaId(pagamento.getReservaId());
        dto.setValor(pagamento.getValor());
        dto.setStatus(pagamento.getStatus());
        dto.setMetodoPagamento(pagamento.getMetodoPagamento());
        dto.setDataCriacao(pagamento.getDataCriacao());
        dto.setDataAtualizacao(pagamento.getDataAtualizacao());
        dto.setDataExpiracao(pagamento.getDataExpiracao());
        dto.setNomeHospede(pagamento.getNomeHospede());
        return dto;
    }
}
