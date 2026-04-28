package br.edu.udesc.reservaservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.udesc.reservaservice.application.dto.CriarReservaCommand;
import br.edu.udesc.reservaservice.application.dto.ReservaDto;
import br.edu.udesc.reservaservice.application.mapper.ReservaMapper;
import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEventPublisher;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEventPublisher;
import br.edu.udesc.reservaservice.domain.exception.AlteracaoStatusInvalidaException;
import br.edu.udesc.reservaservice.domain.exception.DataReservaInvalidaException;
import br.edu.udesc.reservaservice.domain.exception.IntegracaoExternaException;
import br.edu.udesc.reservaservice.domain.model.Hospede;
import br.edu.udesc.reservaservice.domain.model.Reserva;
import br.edu.udesc.reservaservice.domain.repository.HospedeRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaComentarioRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaStatusHistoricoRepository;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.DisponibilidadeQuarto;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.QuartoDisponibilidadeGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private HospedeRepository hospedeRepository;

    @Mock
    private ReservaComentarioRepository reservaComentarioRepository;

    @Mock
    private ReservaStatusHistoricoRepository reservaStatusHistoricoRepository;

    @Mock
    private QuartoDisponibilidadeGateway quartoDisponibilidadeGateway;

    @Mock
    private ReservaDomainEventPublisher reservaDomainEventPublisher;

    @Mock
    private PagamentoReservaCriadaEventPublisher pagamentoReservaCriadaEventPublisher;

    private ReservaService reservaService;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(
            reservaRepository,
            hospedeRepository,
            reservaComentarioRepository,
            reservaStatusHistoricoRepository,
            quartoDisponibilidadeGateway,
            reservaDomainEventPublisher,
            pagamentoReservaCriadaEventPublisher,
            new ReservaMapper()
        );
    }

    @Test
    void deveImpedirCriacaoDeReservaComDatasInvalidas() {
        UUID hospedeId = UUID.randomUUID();
        UUID quartoId = UUID.randomUUID();
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        assertThatThrownBy(() -> reservaService.criar(new CriarReservaCommand(
            hospedeId,
            quartoId,
            null,
            "101",
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(1),
            PagamentoModo.PAGO_NO_HOTEL,
            null,
            null
        ))).isInstanceOf(DataReservaInvalidaException.class);
    }

    @Test
    void deveExecutarFluxoCompletoParaPagamentoNoHotel() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_NO_HOTEL);

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.realizarCheckIn(reserva.getId());
        reservaService.realizarCheckOut(reserva.getId());

        assertThat(reserva.getReservaStatus()).isEqualTo(ReservaStatus.ENCERRADA);
        verify(reservaDomainEventPublisher, times(2)).publicar(any());
    }

    @Test
    void deveExecutarFluxoCompletoParaPagamentoAntecipado() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_ANTECIPADO);

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.confirmarPagamento(reserva.getId());
        reservaService.realizarCheckIn(reserva.getId());
        reservaService.realizarCheckOut(reserva.getId());

        assertThat(reserva.getReservaStatus()).isEqualTo(ReservaStatus.ENCERRADA);
        verify(reservaDomainEventPublisher, times(3)).publicar(any());
    }

    @Test
    void deveImpedirCheckInQuandoPagamentoAntecipadoNaoFoiConfirmado() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_ANTECIPADO);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.realizarCheckIn(reserva.getId()))
            .isInstanceOf(AlteracaoStatusInvalidaException.class);
    }

    @Test
    void deveImpedirCheckOutQuandoReservaNaoFoiIniciada() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_NO_HOTEL);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.realizarCheckOut(reserva.getId()))
            .isInstanceOf(AlteracaoStatusInvalidaException.class);
    }

    @Test
    void devePublicarEventoAoCriarReserva() {
        UUID hospedeId = UUID.randomUUID();
        UUID quartoId = UUID.randomUUID();
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaDto reservaCriada = reservaService.criar(new CriarReservaCommand(
            hospedeId,
            quartoId,
            null,
            "101",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            PagamentoModo.PAGO_NO_HOTEL,
            null,
            null
        ));

        assertThat(reservaCriada.quartoId()).isEqualTo(quartoId);
        assertThat(reservaCriada.quartoServicoId()).isNull();
        verify(reservaDomainEventPublisher).publicar(any());
        verify(quartoDisponibilidadeGateway, never()).verificarDisponibilidadePorServico(any(), any(), any());
        verify(pagamentoReservaCriadaEventPublisher, never()).publicar(any());
        verify(reservaStatusHistoricoRepository).save(any());
    }

    @Test
    void deveValidarQuartoServicoAoCriarReserva() {
        UUID hospedeId = UUID.randomUUID();
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(quartoDisponibilidadeGateway.verificarDisponibilidadePorServico(eq(10L), any(), any()))
            .thenReturn(new DisponibilidadeQuarto(10L, "101", true, false, "ok"));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaDto reservaCriada = reservaService.criar(new CriarReservaCommand(
            hospedeId,
            null,
            10L,
            null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            PagamentoModo.PAGO_NO_HOTEL,
            null,
            null
        ));

        verify(quartoDisponibilidadeGateway).verificarDisponibilidadePorServico(eq(10L), any(), any());
        assertThat(reservaCriada.quartoId()).isNotNull();
        assertThat(reservaCriada.quartoServicoId()).isEqualTo(10L);
        assertThat(reservaCriada.quartoNumero()).isEqualTo("101");
    }

    @Test
    void deveBloquearReservaQuandoQuartoServicoNaoEstiverDisponivel() {
        UUID hospedeId = UUID.randomUUID();
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(quartoDisponibilidadeGateway.verificarDisponibilidadePorServico(eq(10L), any(), any()))
            .thenReturn(new DisponibilidadeQuarto(10L, "101", false, false, "ocupado"));

        assertThatThrownBy(() -> reservaService.criar(new CriarReservaCommand(
            hospedeId,
            null,
            10L,
            null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            PagamentoModo.PAGO_NO_HOTEL,
            null,
            null
        ))).isInstanceOf(IntegracaoExternaException.class);
    }

    @Test
    void devePublicarEventoDePagamentoAoCriarReservaAntecipada() {
        UUID hospedeId = UUID.randomUUID();
        UUID quartoId = UUID.randomUUID();
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.criar(new CriarReservaCommand(
            hospedeId,
            quartoId,
            null,
            "101",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            PagamentoModo.PAGO_ANTECIPADO,
            BigDecimal.valueOf(250),
            MetodoPagamento.PIX
        ));

        verify(pagamentoReservaCriadaEventPublisher).publicar(any());
    }

    @Test
    void deveIgnorarConfirmacaoExternaQuandoReservaJaEstiverPaga() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_ANTECIPADO);
        reserva.confirmarPagamentoAntecipado();
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        reservaService.confirmarPagamentoPorEventoExterno(reserva.getId());

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveConfirmarPagamentoRecebidoPorEventoExterno() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_ANTECIPADO);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.confirmarPagamentoPorEventoExterno(reserva.getId());

        assertThat(reserva.getReservaStatus()).isEqualTo(ReservaStatus.PAGA);
        assertThat(reserva.getPagamentoStatus()).isEqualTo(PagamentoStatus.PAGO);
    }

    @Test
    void deveCancelarReservaQuandoPagamentoExpirar() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_ANTECIPADO);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.cancelarPorPagamentoExpirado(reserva.getId());

        assertThat(reserva.getReservaStatus()).isEqualTo(ReservaStatus.CANCELADA);
        assertThat(reserva.getPagamentoStatus()).isEqualTo(PagamentoStatus.EXPIRADO);
    }

    @Test
    void deveEnviarCheckOutParaServicoDeQuartoQuandoHouverQuartoServicoId() {
        Reserva reserva = criarReserva(PagamentoModo.PAGO_NO_HOTEL, 10L);

        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservaService.realizarCheckIn(reserva.getId());
        reservaService.realizarCheckOut(reserva.getId());

        verify(quartoDisponibilidadeGateway).registrarCheckOut(10L);
    }

    private Reserva criarReserva(PagamentoModo pagamentoModo) {
        return criarReserva(pagamentoModo, null);
    }

    private Reserva criarReserva(PagamentoModo pagamentoModo, Long quartoServicoId) {
        Hospede hospede = new Hospede("João", "12345678909", "joao@email.com", "48999998888");
        return new Reserva(
            hospede,
            UUID.randomUUID(),
            quartoServicoId,
            "101",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            pagamentoModo,
            pagamentoModo == PagamentoModo.PAGO_ANTECIPADO ? BigDecimal.valueOf(500) : null,
            pagamentoModo == PagamentoModo.PAGO_ANTECIPADO ? MetodoPagamento.PIX : null
        );
    }
}
