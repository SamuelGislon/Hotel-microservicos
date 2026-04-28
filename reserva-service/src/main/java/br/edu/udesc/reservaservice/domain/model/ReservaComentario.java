package br.edu.udesc.reservaservice.domain.model;

import br.edu.udesc.reservaservice.shared.base.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva_comentarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservaComentario extends EntidadeAuditavel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(nullable = false, length = 1000)
    private String comentario;

    public ReservaComentario(Reserva reserva, String comentario) {
        this.id = UUID.randomUUID();
        this.reserva = reserva;
        this.comentario = comentario;
    }

    @PrePersist
    void garantirId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
