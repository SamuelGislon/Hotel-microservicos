package com.quartos.api_quartos.model;

public enum TipoQuarto {
    SIMPLES("Cama de Solteiro", 150),
    DUPLO ("Cama de Casal", 150),
    SUITE("Acomoda múltiplas pessoas", 200);

    private final String descricao;
    private final double precoPorNoite;

    TipoQuarto(String descricao, double preco) {
        this.descricao = descricao;
        this.precoPorNoite = preco;
    }
    public String getDescricao() {
        return descricao;
    }

    public double getPrecoPorNoite() {
        return precoPorNoite;
    }
}
