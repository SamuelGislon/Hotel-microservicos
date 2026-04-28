package com.hosped.ms_pagamentos;

import com.hosped.ms_pagamentos.dto.ReservaEventoDTO;
import com.hosped.ms_pagamentos.email.EmailService;
import com.hosped.ms_pagamentos.model.MetodoPagamento;
import com.hosped.ms_pagamentos.model.Pagamento;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import com.hosped.ms_pagamentos.repository.PagamentoRepository;
import com.hosped.ms_pagamentos.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PagamentoService pagamentoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagamentoService, "horasExpiracao", 8);
    }

    private ReservaEventoDTO criarEvento() {
        ReservaEventoDTO evento = new ReservaEventoDTO();
        evento.setReservaId("reserva-001");
        evento.setNomeHospede("João Silva");
        evento.setEmailHospede("joao@email.com");
        evento.setValorDiaria(new BigDecimal("200.00"));
        evento.setCheckIn(LocalDate.of(2025, 6, 10));
        evento.setCheckOut(LocalDate.of(2025, 6, 13));
        evento.setMetodoPagamento(MetodoPagamento.PIX);
        return evento;
    }

    private Pagamento criarPagamentoPendente() {
        Pagamento p = new Pagamento();
        p.setId("pag-001");
        p.setReservaId("reserva-001");
        p.setValor(new BigDecimal("600.00"));
        p.setStatus(StatusPagamento.PENDENTE);
        p.setMetodoPagamento(MetodoPagamento.PIX);
        p.setNomeHospede("João Silva");
        p.setEmailHospede("joao@email.com");
        p.setDataCriacao(LocalDateTime.now());
        p.setDataExpiracao(LocalDateTime.now().plusHours(8));
        return p;
    }

    // ───── processarReserva ─────

    @Test
    void processarReserva_calculaValorCorretamente() {
        ReservaEventoDTO evento = criarEvento();
        Pagamento salvo = criarPagamentoPendente();

        when(pagamentoRepository.save(any())).thenReturn(salvo);

        Pagamento resultado = pagamentoService.processarReserva(evento);

        assertEquals(new BigDecimal("600.00"), resultado.getValor());
    }

    @Test
    void processarReserva_criaPagamentoPendente() {
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pagamento resultado = pagamentoService.processarReserva(criarEvento());

        assertEquals(StatusPagamento.PENDENTE, resultado.getStatus());
    }

    @Test
    void processarReserva_salvaNoRepositorio() {
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.processarReserva(criarEvento());

        verify(pagamentoRepository).save(any());
    }

    @Test
    void processarReserva_enviEmailPendente() {
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.processarReserva(criarEvento());

        verify(emailService).enviarEmailPagamentoPendente(any());
    }

    @Test
    void processarReserva_defineDataExpiracao() {
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pagamento resultado = pagamentoService.processarReserva(criarEvento());

        assertNotNull(resultado.getDataExpiracao());
        assertTrue(resultado.getDataExpiracao().isAfter(LocalDateTime.now()));
    }

    // ───── confirmarPagamento ─────

    @Test
    void confirmarPagamento_aprovaPagamentoPendente() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.confirmarPagamento("pag-001");

        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
    }

    @Test
    void confirmarPagamento_publicaEventoRabbit() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.confirmarPagamento("pag-001");

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void confirmarPagamento_enviEmailAprovado() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.confirmarPagamento("pag-001");

        verify(emailService).enviarEmailPagamentoAprovado(any());
    }

    @Test
    void confirmarPagamento_lancaErroSeJaAprovado() {
        Pagamento pagamento = criarPagamentoPendente();
        pagamento.setStatus(StatusPagamento.APROVADO);
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));

        assertThrows(ResponseStatusException.class, () ->
                pagamentoService.confirmarPagamento("pag-001"));
    }

    @Test
    void confirmarPagamento_lancaErroSeExpirado() {
        Pagamento pagamento = criarPagamentoPendente();
        pagamento.setDataExpiracao(LocalDateTime.now().minusHours(1));
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));

        assertThrows(ResponseStatusException.class, () ->
                pagamentoService.confirmarPagamento("pag-001"));
    }

    @Test
    void confirmarPagamento_lancaErroSeNaoEncontrado() {
        when(pagamentoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                pagamentoService.confirmarPagamento("inexistente"));
    }

    // ───── atualizarStatus ─────

    @Test
    void atualizarStatus_paraAprovado_enviEmailAprovado() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.atualizarStatus("pag-001", StatusPagamento.APROVADO);

        verify(emailService).enviarEmailPagamentoAprovado(any());
    }

    @Test
    void atualizarStatus_paraExpirado_enviEmailExpirado() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.atualizarStatus("pag-001", StatusPagamento.EXPIRADO);

        verify(emailService).enviarEmailPagamentoExpirado(any());
    }

    @Test
    void atualizarStatus_publicaEventoRabbit() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.atualizarStatus("pag-001", StatusPagamento.APROVADO);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void atualizarStatus_lancaErroSeNaoEncontrado() {
        when(pagamentoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                pagamentoService.atualizarStatus("inexistente", StatusPagamento.APROVADO));
    }

    // ───── buscarPorId ─────

    @Test
    void buscarPorId_retornaDtoCorreto() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findById("pag-001")).thenReturn(Optional.of(pagamento));

        var dto = pagamentoService.buscarPorId("pag-001");

        assertEquals("pag-001", dto.getId());
        assertEquals("reserva-001", dto.getReservaId());
        assertEquals(new BigDecimal("600.00"), dto.getValor());
    }

    @Test
    void buscarPorId_lancaErroSeNaoEncontrado() {
        when(pagamentoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                pagamentoService.buscarPorId("inexistente"));
    }

    // ───── buscarPorReservaId ─────

    @Test
    void buscarPorReservaId_retornaListaCorreta() {
        Pagamento pagamento = criarPagamentoPendente();
        when(pagamentoRepository.findByReservaId("reserva-001")).thenReturn(List.of(pagamento));

        var lista = pagamentoService.buscarPorReservaId("reserva-001");

        assertEquals(1, lista.size());
        assertEquals("reserva-001", lista.get(0).getReservaId());
    }

    @Test
    void buscarPorReservaId_retornaListaVaziaSeNaoHouver() {
        when(pagamentoRepository.findByReservaId("reserva-999")).thenReturn(List.of());

        var lista = pagamentoService.buscarPorReservaId("reserva-999");

        assertTrue(lista.isEmpty());
    }
}