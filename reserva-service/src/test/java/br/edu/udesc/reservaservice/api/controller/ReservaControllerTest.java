package br.edu.udesc.reservaservice.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.udesc.reservaservice.application.dto.ReservaDto;
import br.edu.udesc.reservaservice.application.mapper.RespostaApiMapper;
import br.edu.udesc.reservaservice.application.service.ReservaService;
import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import br.edu.udesc.reservaservice.domain.exception.AlteracaoStatusInvalidaException;
import br.edu.udesc.reservaservice.exception.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservaController.class)
@Import({RespostaApiMapper.class, ApiExceptionHandler.class})
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaService reservaService;

    @Test
    void deveCriarReserva() throws Exception {
        UUID reservaId = UUID.randomUUID();
        UUID hospedeId = UUID.randomUUID();
        UUID quartoId = UUID.randomUUID();

        when(reservaService.criar(any())).thenReturn(new ReservaDto(
            reservaId,
            hospedeId,
            "João da Silva",
            quartoId,
            null,
            "101",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            ReservaStatus.PENDENTE,
            PagamentoModo.PAGO_NO_HOTEL,
            PagamentoStatus.NAO_APLICAVEL,
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        ));

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("hospedeId", hospedeId);
        payload.put("quartoId", quartoId);
        payload.put("quartoNumero", "101");
        payload.put("checkInData", LocalDate.now().plusDays(1).toString());
        payload.put("checkOutData", LocalDate.now().plusDays(2).toString());
        payload.put("pagamentoModo", "PAGO_NO_HOTEL");

        mockMvc.perform(post("/api/v1/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(reservaId.toString()))
            .andExpect(jsonPath("$.reservaStatus").value("PENDENTE"));
    }

    @Test
    void deveRetornarErroQuandoCheckInForInvalido() throws Exception {
        UUID reservaId = UUID.randomUUID();
        when(reservaService.realizarCheckIn(reservaId))
            .thenThrow(new AlteracaoStatusInvalidaException("Check-in inválido"));

        mockMvc.perform(post("/api/v1/reservas/{id}/check-in", reservaId))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Check-in inválido"));
    }
}
