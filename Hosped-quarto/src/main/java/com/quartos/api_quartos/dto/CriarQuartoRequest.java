package com.quartos.api_quartos.dto;

import com.quartos.api_quartos.model.TipoQuarto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarQuartoRequest(
        @NotNull(message = "Número do quarto é obrigatório")
        @Positive(message = "Número do quarto deve ser positivo")
        Integer numeroQuarto,

        @NotNull(message = "Tipo do quarto é obrigatório")
        TipoQuarto tipo,

        @NotNull(message = "Capacidade é obrigatória")
        @Positive(message = "Capacidade deve ser positiva")
        Integer capacidade
) {
}
