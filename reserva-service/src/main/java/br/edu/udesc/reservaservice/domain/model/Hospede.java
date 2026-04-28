package br.edu.udesc.reservaservice.domain.model;

import br.edu.udesc.reservaservice.shared.base.EntidadeAuditavel;
import br.edu.udesc.reservaservice.shared.util.CpfUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hospedes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hospede extends EntidadeAuditavel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome_completo", nullable = false, length = 160)
    private String nomeCompleto;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 30)
    private String telefone;

    @Column(nullable = false)
    private boolean ativo;

    public Hospede(String nomeCompleto, String cpf, String email, String telefone) {
        this.id = UUID.randomUUID();
        this.nomeCompleto = nomeCompleto;
        this.cpf = CpfUtils.normalizar(cpf);
        this.email = email;
        this.telefone = telefone;
        this.ativo = true;
    }

    @PrePersist
    void garantirId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public void atualizar(String nomeCompleto, String cpf, String email, String telefone, Boolean ativo) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = CpfUtils.normalizar(cpf);
        this.email = email;
        this.telefone = telefone;
        if (ativo != null) {
            this.ativo = ativo;
        }
    }
}
