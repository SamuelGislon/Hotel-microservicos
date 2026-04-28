package br.edu.udesc.reservaservice.domain.model;

import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import br.edu.udesc.reservaservice.domain.exception.AlteracaoStatusInvalidaException;
import br.edu.udesc.reservaservice.domain.exception.DataReservaInvalidaException;
import br.edu.udesc.reservaservice.shared.base.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserva extends EntidadeAuditavel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospede_id", nullable = false)
    private Hospede hospede;

    @Column(name = "quarto_id", nullable = false)
    private UUID quartoId;

    @Column(name = "quarto_servico_id")
    private Long quartoServicoId;

    @Column(name = "quarto_numero", length = 40)
    private String quartoNumero;

    @Column(name = "check_in_data", nullable = false)
    private LocalDate checkInData;

    @Column(name = "check_out_data", nullable = false)
    private LocalDate checkOutData;

    @Enumerated(EnumType.STRING)
    @Column(name = "reserva_status", nullable = false, length = 40)
    private ReservaStatus reservaStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pagamento_modo", nullable = false, length = 40)
    private PagamentoModo pagamentoModo;

    @Enumerated(EnumType.STRING)
    @Column(name = "pagamento_status", nullable = false, length = 40)
    private PagamentoStatus pagamentoStatus;

    @Column(name = "valor_diaria", precision = 12, scale = 2)
    private BigDecimal valorDiaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", length = 40)
    private MetodoPagamento metodoPagamento;

    @Column(name = "check_in_realizado_at")
    private LocalDateTime checkInRealizadoAt;

    @Column(name = "check_out_realizado_at")
    private LocalDateTime checkOutRealizadoAt;

    public Reserva(
        Hospede hospede,
        UUID quartoId,
        String quartoNumero,
        LocalDate checkInData,
        LocalDate checkOutData,
        PagamentoModo pagamentoModo
    ) {
        this(hospede, quartoId, null, quartoNumero, checkInData, checkOutData, pagamentoModo, null, null);
    }

    public Reserva(
        Hospede hospede,
        UUID quartoId,
        Long quartoServicoId,
        String quartoNumero,
        LocalDate checkInData,
        LocalDate checkOutData,
        PagamentoModo pagamentoModo,
        BigDecimal valorDiaria,
        MetodoPagamento metodoPagamento
    ) {
        this.id = UUID.randomUUID();
        this.hospede = hospede;
        this.quartoId = quartoId != null ? quartoId : UUID.randomUUID();
        this.quartoServicoId = quartoServicoId;
        this.quartoNumero = quartoNumero;
        this.checkInData = checkInData;
        this.checkOutData = checkOutData;
        this.pagamentoModo = pagamentoModo;
        this.valorDiaria = valorDiaria;
        this.metodoPagamento = metodoPagamento;
        this.reservaStatus = ReservaStatus.PENDENTE;
        this.pagamentoStatus = pagamentoModo == PagamentoModo.PAGO_ANTECIPADO
            ? PagamentoStatus.PENDENTE
            : PagamentoStatus.NAO_APLICAVEL;
        validarDatas();
    }

    @PrePersist
    void garantirId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public void validarDatas() {
        if (checkInData == null || checkOutData == null || !checkOutData.isAfter(checkInData)) {
            throw new DataReservaInvalidaException();
        }
    }

    public ReservaStatus confirmarPagamentoAntecipado() {
        if (pagamentoModo != PagamentoModo.PAGO_ANTECIPADO) {
            throw new AlteracaoStatusInvalidaException("Somente reservas com pagamento antecipado podem ser confirmadas");
        }
        if (reservaStatus == ReservaStatus.PAGA || pagamentoStatus == PagamentoStatus.PAGO) {
            return reservaStatus;
        }
        if (reservaStatus != ReservaStatus.PENDENTE) {
            throw new AlteracaoStatusInvalidaException(
                "A confirmação de pagamento só é permitida para reservas pendentes de pagamento"
            );
        }
        reservaStatus = ReservaStatus.PAGA;
        pagamentoStatus = PagamentoStatus.PAGO;
        return reservaStatus;
    }

    public ReservaStatus cancelarPorPagamentoExpirado() {
        if (reservaStatus == ReservaStatus.ATIVA || reservaStatus == ReservaStatus.ENCERRADA) {
            throw new AlteracaoStatusInvalidaException("Não é permitido cancelar reserva já iniciada ou encerrada");
        }
        if (reservaStatus == ReservaStatus.CANCELADA) {
            return reservaStatus;
        }
        reservaStatus = ReservaStatus.CANCELADA;
        pagamentoStatus = PagamentoStatus.EXPIRADO;
        return reservaStatus;
    }

    public ReservaStatus realizarCheckIn() {
        if (reservaStatus == ReservaStatus.ENCERRADA) {
            throw new AlteracaoStatusInvalidaException("Não é permitido realizar check-in em reserva já encerrada");
        }
        if (pagamentoModo == PagamentoModo.PAGO_ANTECIPADO && reservaStatus != ReservaStatus.PAGA) {
            throw new AlteracaoStatusInvalidaException(
                "Reservas com pagamento antecipado exigem confirmação de pagamento antes do check-in"
            );
        }
        if (pagamentoModo == PagamentoModo.PAGO_NO_HOTEL && reservaStatus != ReservaStatus.PENDENTE) {
            throw new AlteracaoStatusInvalidaException("O check-in só é permitido para reservas pendentes");
        }
        if (pagamentoModo == PagamentoModo.PAGO_ANTECIPADO && reservaStatus == ReservaStatus.PAGA) {
            reservaStatus = ReservaStatus.ATIVA;
            checkInRealizadoAt = LocalDateTime.now();
            return reservaStatus;
        }
        if (pagamentoModo == PagamentoModo.PAGO_NO_HOTEL && reservaStatus == ReservaStatus.PENDENTE) {
            reservaStatus = ReservaStatus.ATIVA;
            checkInRealizadoAt = LocalDateTime.now();
            return reservaStatus;
        }

        throw new AlteracaoStatusInvalidaException("Transição de status inválida para check-in");
    }

    public ReservaStatus realizarCheckOut() {
        if (reservaStatus == ReservaStatus.PENDENTE || reservaStatus == ReservaStatus.PAGA) {
            throw new AlteracaoStatusInvalidaException("Não é permitido realizar check-out em reserva ainda não iniciada");
        }
        if (reservaStatus != ReservaStatus.ATIVA) {
            throw new AlteracaoStatusInvalidaException("O check-out só é permitido para reservas ativas");
        }
        reservaStatus = ReservaStatus.ENCERRADA;
        checkOutRealizadoAt = LocalDateTime.now();
        return reservaStatus;
    }

    public boolean aceitaComentarioEncerramento() {
        return reservaStatus == ReservaStatus.ENCERRADA;
    }
}
