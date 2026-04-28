package br.edu.udesc.reservaservice.api.request;

import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarReservaRequest(
    @NotNull(message = "O hóspede responsável é obrigatório")
    UUID hospedeId,

    UUID quartoId,

    @Positive(message = "O id de integração do quarto deve ser maior que zero")
    Long quartoServicoId,

    @Size(max = 40, message = "O número do quarto deve ter no máximo 40 caracteres")
    String quartoNumero,

    @NotNull(message = "A data de check-in é obrigatória")
    @FutureOrPresent(message = "A data de check-in deve ser hoje ou uma data futura")
    LocalDate checkInData,

    @NotNull(message = "A data de check-out é obrigatória")
    LocalDate checkOutData,

    @NotNull(message = "A modalidade de pagamento é obrigatória")
    PagamentoModo pagamentoModo,

    @Positive(message = "O valor da diária deve ser maior que zero")
    BigDecimal valorDiaria,

    MetodoPagamento metodoPagamento
) {
}
