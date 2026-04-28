package br.edu.udesc.reservaservice.domain.model;

import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva_status_historico")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservaStatusHistorico {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 40)
    private ReservaStatus statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 40)
    private ReservaStatus statusNovo;

    @Column(length = 255)
    private String motivo;

    @Column(name = "atualizado_at", nullable = false)
    private LocalDateTime atualizadoAt;

    public ReservaStatusHistorico(Reserva reserva, ReservaStatus statusAnterior, ReservaStatus statusNovo, String motivo) {
        this.id = UUID.randomUUID();
        this.reserva = reserva;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = motivo;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        atualizadoAt = LocalDateTime.now();
    }
}
