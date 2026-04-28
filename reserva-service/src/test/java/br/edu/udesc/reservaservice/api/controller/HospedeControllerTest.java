package br.edu.udesc.reservaservice.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.udesc.reservaservice.application.dto.HospedeDto;
import br.edu.udesc.reservaservice.application.mapper.RespostaApiMapper;
import br.edu.udesc.reservaservice.application.service.HospedeService;
import br.edu.udesc.reservaservice.domain.exception.HospedeNaoEncontradoException;
import br.edu.udesc.reservaservice.exception.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HospedeController.class)
@Import({RespostaApiMapper.class, ApiExceptionHandler.class})
class HospedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HospedeService hospedeService;

    @Test
    void deveCadastrarHospede() throws Exception {
        UUID hospedeId = UUID.randomUUID();
        when(hospedeService.cadastrar(any())).thenReturn(new HospedeDto(
            hospedeId,
            "Maria Silva",
            "12345678909",
            "maria@email.com",
            "48999999999",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/hospedes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                    put("nomeCompleto", "Maria Silva");
                    put("cpf", "12345678909");
                    put("email", "maria@email.com");
                    put("telefone", "48999999999");
                }})))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(hospedeId.toString()))
            .andExpect(jsonPath("$.nomeCompleto").value("Maria Silva"));
    }

    @Test
    void deveRetornarNotFoundQuandoHospedeNaoExistir() throws Exception {
        UUID hospedeId = UUID.randomUUID();
        when(hospedeService.buscarPorId(hospedeId)).thenThrow(new HospedeNaoEncontradoException(hospedeId));

        mockMvc.perform(get("/api/v1/hospedes/{id}", hospedeId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists());
    }
}
