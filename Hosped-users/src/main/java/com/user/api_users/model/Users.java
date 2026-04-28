package com.user.api_users.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private Cargos cargo;

    @Column(unique = true, nullable = false)
    private String cpf;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    public Users(){
    }

    public Users(String nome, Cargos cargo, String cpf, String senha){
        this.nome = nome;
        this.cargo = cargo;
        this.cpf = cpf;
        this.senha = senha;
    }
}

