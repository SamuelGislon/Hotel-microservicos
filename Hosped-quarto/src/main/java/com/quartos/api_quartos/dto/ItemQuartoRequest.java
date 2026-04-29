package com.quartos.api_quartos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ItemQuartoRequest(
        @NotBlank(message = "Nome do item é obrigatório")
        String nomeItem,

        @Positive(message = "Quantidade deve ser maior que zero")
        int quantidade
) {
}
