package br.edu.udesc.reservaservice.domain.model;

import br.edu.udesc.reservaservice.domain.enums.StatusPublicacaoEvento;
import br.edu.udesc.reservaservice.shared.base.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evento_publicacao_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventoPublicacaoLog extends EntidadeAuditavel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tipo_evento", nullable = false, length = 120)
    private String tipoEvento;

    @Column(name = "agregado_id", nullable = false)
    private UUID agregadoId;

    @Column(name = "payload_resumo", nullable = false, length = 4000)
    private String payloadResumo;

    @Column(name = "publicado_em", nullable = false)
    private LocalDateTime publicadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_publicacao", nullable = false, length = 20)
    private StatusPublicacaoEvento statusPublicacao;

    @Column(name = "mensagem_erro", length = 1000)
    private String mensagemErro;

    public EventoPublicacaoLog(String tipoEvento, UUID agregadoId, String payloadResumo, StatusPublicacaoEvento statusPublicacao) {
        this.id = UUID.randomUUID();
        this.tipoEvento = tipoEvento;
        this.agregadoId = agregadoId;
        this.payloadResumo = payloadResumo;
        this.statusPublicacao = statusPublicacao;
        this.publicadoEm = LocalDateTime.now();
    }

    @PrePersist
    void garantirId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (publicadoEm == null) {
            publicadoEm = LocalDateTime.now();
        }
    }

    public void marcarFalha(String mensagemErro) {
        this.statusPublicacao = StatusPublicacaoEvento.FALHA;
        this.mensagemErro = mensagemErro;
        this.publicadoEm = LocalDateTime.now();
    }

    public void marcarSucesso() {
        this.statusPublicacao = StatusPublicacaoEvento.SUCESSO;
        this.mensagemErro = null;
        this.publicadoEm = LocalDateTime.now();
    }
}
