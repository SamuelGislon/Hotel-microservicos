package br.edu.udesc.reservaservice.application.service;

import br.edu.udesc.reservaservice.application.dto.CriarReservaCommand;
import br.edu.udesc.reservaservice.application.dto.ReservaComentarioDto;
import br.edu.udesc.reservaservice.application.dto.ReservaDto;
import br.edu.udesc.reservaservice.application.mapper.ReservaMapper;
import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEvent;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEventPublisher;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEvent;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEventPublisher;
import br.edu.udesc.reservaservice.domain.exception.HospedeNaoEncontradoException;
import br.edu.udesc.reservaservice.domain.exception.IntegracaoExternaException;
import br.edu.udesc.reservaservice.domain.exception.RegraDeNegocioException;
import br.edu.udesc.reservaservice.domain.exception.ReservaNaoEncontradaException;
import br.edu.udesc.reservaservice.domain.model.Hospede;
import br.edu.udesc.reservaservice.domain.model.Reserva;
import br.edu.udesc.reservaservice.domain.model.ReservaComentario;
import br.edu.udesc.reservaservice.domain.model.ReservaStatusHistorico;
import br.edu.udesc.reservaservice.domain.repository.HospedeRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaComentarioRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaStatusHistoricoRepository;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.DisponibilidadeQuarto;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.QuartoDisponibilidadeGateway;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HospedeRepository hospedeRepository;
    private final ReservaComentarioRepository reservaComentarioRepository;
    private final ReservaStatusHistoricoRepository reservaStatusHistoricoRepository;
    private final QuartoDisponibilidadeGateway quartoDisponibilidadeGateway;
    private final ReservaDomainEventPublisher reservaDomainEventPublisher;
    private final PagamentoReservaCriadaEventPublisher pagamentoReservaCriadaEventPublisher;
    private final ReservaMapper reservaMapper;

    @Transactional
    public ReservaDto criar(CriarReservaCommand command) {
        validarCriacao(command);

        Hospede hospede = hospedeRepository.findById(command.hospedeId())
            .orElseThrow(() -> new HospedeNaoEncontradoException(command.hospedeId()));

        DisponibilidadeQuarto disponibilidade = validarQuarto(command);
        String quartoNumero = resolverQuartoNumero(command, disponibilidade);

        Reserva reserva = new Reserva(
            hospede,
            command.quartoId(),
            command.quartoServicoId(),
            quartoNumero,
            command.checkInData(),
            command.checkOutData(),
            command.pagamentoModo(),
            command.pagamentoModo() == PagamentoModo.PAGO_ANTECIPADO ? command.valorDiaria() : null,
            command.pagamentoModo() == PagamentoModo.PAGO_ANTECIPADO ? command.metodoPagamento() : null
        );

        Reserva reservaSalva = reservaRepository.save(reserva);
        registrarHistorico(reservaSalva, null, reservaSalva.getReservaStatus(), "Reserva criada");
        reservaDomainEventPublisher.publicar(ReservaDomainEvent.reservaCriada(reservaSalva));
        if (reservaSalva.getPagamentoModo() == PagamentoModo.PAGO_ANTECIPADO) {
            pagamentoReservaCriadaEventPublisher.publicar(PagamentoReservaCriadaEvent.from(reservaSalva));
            log.info("Reserva criada aguardando pagamento antecipado. reservaId={}", reservaSalva.getId());
        } else {
            log.info("Reserva criada com pagamento no balcão. reservaId={}", reservaSalva.getId());
        }
        return reservaMapper.toDto(reservaSalva);
    }

    @Transactional(readOnly = true)
    public List<ReservaDto> listar() {
        return reservaRepository.findAll()
            .stream()
            .map(reservaMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public ReservaDto buscarPorId(UUID reservaId) {
        return reservaMapper.toDto(buscarReserva(reservaId));
    }

    @Transactional(readOnly = true)
    public List<ReservaDto> listarPorHospede(UUID hospedeId) {
        return reservaRepository.findByHospedeIdOrderByCriadoAtDesc(hospedeId)
            .stream()
            .map(reservaMapper::toDto)
            .toList();
    }

    @Transactional
    public ReservaDto realizarCheckIn(UUID reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        ReservaStatus anterior = reserva.getReservaStatus();
        reserva.realizarCheckIn();
        Reserva reservaAtualizada = reservaRepository.save(reserva);
        registrarHistorico(reservaAtualizada, anterior, reservaAtualizada.getReservaStatus(), "Check-in realizado");
        reservaDomainEventPublisher.publicar(ReservaDomainEvent.checkInRealizado(reservaAtualizada));
        log.info(
            "Check-in realizado. reservaId={}, quartoServicoId={}. Integração para marcar OCUPADO indisponível no Hosped-quarto.",
            reservaAtualizada.getId(),
            reservaAtualizada.getQuartoServicoId()
        );
        return reservaMapper.toDto(reservaAtualizada);
    }

    @Transactional
    public ReservaDto realizarCheckOut(UUID reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        ReservaStatus anterior = reserva.getReservaStatus();
        reserva.realizarCheckOut();
        Reserva reservaAtualizada = reservaRepository.save(reserva);
        registrarHistorico(reservaAtualizada, anterior, reservaAtualizada.getReservaStatus(), "Check-out realizado");
        reservaDomainEventPublisher.publicar(ReservaDomainEvent.checkOutRealizado(reservaAtualizada));
        registrarCheckOutQuarto(reservaAtualizada);
        return reservaMapper.toDto(reservaAtualizada);
    }

    @Transactional
    public ReservaDto confirmarPagamento(UUID reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        ReservaStatus anterior = reserva.getReservaStatus();
        reserva.confirmarPagamentoAntecipado();
        if (anterior == reserva.getReservaStatus()) {
            return reservaMapper.toDto(reserva);
        }
        Reserva reservaAtualizada = reservaRepository.save(reserva);
        registrarHistorico(
            reservaAtualizada,
            anterior,
            reservaAtualizada.getReservaStatus(),
            "Pagamento antecipado confirmado"
        );
        reservaDomainEventPublisher.publicar(ReservaDomainEvent.pagamentoConfirmado(reservaAtualizada));
        log.info("Pagamento confirmado para reserva. reservaId={}", reservaAtualizada.getId());
        return reservaMapper.toDto(reservaAtualizada);
    }

    @Transactional
    public void confirmarPagamentoPorEventoExterno(UUID reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        if (reserva.getPagamentoModo() != PagamentoModo.PAGO_ANTECIPADO) {
            return;
        }
        if (reserva.getReservaStatus() == ReservaStatus.PAGA || reserva.getReservaStatus() == ReservaStatus.ATIVA
            || reserva.getReservaStatus() == ReservaStatus.ENCERRADA
            || reserva.getReservaStatus() == ReservaStatus.CANCELADA) {
            return;
        }
        confirmarPagamento(reservaId);
    }

    @Transactional
    public void cancelarPorPagamentoExpirado(UUID reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        if (reserva.getPagamentoModo() != PagamentoModo.PAGO_ANTECIPADO) {
            return;
        }
        if (reserva.getReservaStatus() == ReservaStatus.PAGA || reserva.getReservaStatus() == ReservaStatus.ATIVA
            || reserva.getReservaStatus() == ReservaStatus.ENCERRADA) {
            return;
        }

        ReservaStatus anterior = reserva.getReservaStatus();
        reserva.cancelarPorPagamentoExpirado();
        if (anterior == reserva.getReservaStatus()) {
            return;
        }

        Reserva reservaAtualizada = reservaRepository.save(reserva);
        registrarHistorico(reservaAtualizada, anterior, reservaAtualizada.getReservaStatus(), "Pagamento expirado");
        log.info("Reserva cancelada por expiração de pagamento. reservaId={}", reservaAtualizada.getId());
    }

    @Transactional
    public ReservaComentarioDto registrarComentario(UUID reservaId, String comentario) {
        Reserva reserva = buscarReserva(reservaId);
        if (!reserva.aceitaComentarioEncerramento()) {
            throw new RegraDeNegocioException("Comentários de encerramento só podem ser registrados para reservas encerradas");
        }
        ReservaComentario comentarioSalvo = reservaComentarioRepository.save(new ReservaComentario(reserva, comentario));
        return reservaMapper.toDto(comentarioSalvo);
    }

    private Reserva buscarReserva(UUID reservaId) {
        return reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));
    }

    private void validarCriacao(CriarReservaCommand command) {
        if (command.quartoId() == null && command.quartoServicoId() == null) {
            throw new RegraDeNegocioException("Informe quartoId ou quartoServicoId para criar a reserva");
        }
        if (command.valorDiaria() != null && command.valorDiaria().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor da diária deve ser maior que zero");
        }
        if (command.pagamentoModo() == PagamentoModo.PAGO_ANTECIPADO) {
            if (command.valorDiaria() == null) {
                throw new RegraDeNegocioException("O valor da diária é obrigatório para pagamento antecipado");
            }
            if (command.metodoPagamento() == null) {
                throw new RegraDeNegocioException("O método de pagamento é obrigatório para pagamento antecipado");
            }
        }
    }

    private DisponibilidadeQuarto validarQuarto(CriarReservaCommand command) {
        if (command.quartoServicoId() != null) {
            DisponibilidadeQuarto disponibilidade = quartoDisponibilidadeGateway.verificarDisponibilidadePorServico(
                command.quartoServicoId(),
                command.checkInData(),
                command.checkOutData()
            );
            if (!disponibilidade.disponivel()) {
                throw new IntegracaoExternaException(disponibilidade.mensagem());
            }
            return disponibilidade;
        }
        return new DisponibilidadeQuarto(
            command.quartoId(),
            true,
            false,
            "Validação externa não aplicada para quartoId legado"
        );
    }

    private String resolverQuartoNumero(CriarReservaCommand command, DisponibilidadeQuarto disponibilidade) {
        if (command.quartoNumero() != null && !command.quartoNumero().isBlank()) {
            return command.quartoNumero();
        }
        return disponibilidade != null ? disponibilidade.quartoNumero() : null;
    }

    private void registrarCheckOutQuarto(Reserva reserva) {
        if (reserva.getQuartoServicoId() == null) {
            return;
        }
        try {
            quartoDisponibilidadeGateway.registrarCheckOut(reserva.getQuartoServicoId());
            log.info("Check-out enviado ao serviço de quartos. reservaId={}, quartoServicoId={}", reserva.getId(), reserva.getQuartoServicoId());
        } catch (Exception exception) {
            log.warn("Falha ao enviar check-out ao serviço de quartos. reservaId={}, quartoServicoId={}", reserva.getId(), reserva.getQuartoServicoId(), exception);
        }
    }

    private void registrarHistorico(Reserva reserva, ReservaStatus statusAnterior, ReservaStatus statusNovo, String motivo) {
        reservaStatusHistoricoRepository.save(new ReservaStatusHistorico(reserva, statusAnterior, statusNovo, motivo));
    }
}
