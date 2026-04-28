package com.quartos.api_quartos.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.*;

@Getter
@Setter
@Entity
@Table(name = "itens_quarto")
public class ItemQuarto {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long idItem;

    private String nomeItem;
    private int quantidade;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    public ItemQuarto(){}

    public ItemQuarto(String nomeItem, int quantidade){
        this.nomeItem = nomeItem;
        this.quantidade = quantidade;
    }
}
