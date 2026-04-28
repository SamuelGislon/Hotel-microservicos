package com.quartos.api_quartos.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "quarto")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quarto_id", nullable = false)
    private Long id;

    @JsonManagedReference
    @OneToMany(mappedBy = "quarto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemQuarto> itens = new ArrayList<>();

    private int numeroQuarto;

    private int capacidade;

    @Enumerated(EnumType.STRING)
    private TipoQuarto tipo;

    @Enumerated(EnumType.STRING)
    private StatusQuarto status = StatusQuarto.DISPONIVEL;

    public Quarto() {}

    public Quarto(int numeroQuarto, TipoQuarto tipo, int capacidade) {
        this.numeroQuarto = numeroQuarto;
        this.tipo = tipo;
        this.capacidade = capacidade;
    }

    public void addItem(ItemQuarto item) {
        item.setQuarto(this);
        itens.add(item);
    }

    public void removeItem(ItemQuarto item) {
        item.setQuarto(null);
        itens.remove(item);
    }
}