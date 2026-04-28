package br.edu.udesc.reservaservice.shared.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class EntidadeAuditavel {

    @Column(name = "criado_at", nullable = false, updatable = false)
    private LocalDateTime criadoAt;

    @Column(name = "atualizado_at", nullable = false)
    private LocalDateTime atualizadoAt;

    @PrePersist
    protected void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        criadoAt = agora;
        atualizadoAt = agora;
    }

    @PreUpdate
    protected void preUpdate() {
        atualizadoAt = LocalDateTime.now();
    }
}
