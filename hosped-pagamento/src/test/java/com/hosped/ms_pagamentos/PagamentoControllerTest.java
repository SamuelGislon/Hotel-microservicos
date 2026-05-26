package com.hosped.ms_pagamentos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosped.ms_pagamentos.controller.PagamentoController;
import com.hosped.ms_pagamentos.dto.ReservaEventoDTO;
import com.hosped.ms_pagamentos.dto.StatusPagamentoIntegracaoDTO;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import com.hosped.ms_pagamentos.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoService pagamentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveConsultarStatusIntegracao() throws Exception {

        StatusPagamentoIntegracaoDTO dto =
                new StatusPagamentoIntegracaoDTO(
                        "reserva-001",
                        "APROVADO",
                        false,
                        "Integração OK"
                );

        when(pagamentoService.consultarStatusIntegracao("reserva-001"))
                .thenReturn(dto);

        mockMvc.perform(get("/pagamentos/reserva/reserva-001/status-integracao"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarStatus() throws Exception {

        mockMvc.perform(
                        patch("/pagamentos/pag-001/status")
                                .param("status", "APROVADO")
                )
                .andExpect(status().isNoContent());

        verify(pagamentoService)
                .atualizarStatus("pag-001", StatusPagamento.APROVADO);
    }

    @Test
    void deveConfirmarPagamentoPorReserva() throws Exception {

        mockMvc.perform(
                        post("/pagamentos/reserva/reserva-001/confirmar")
                )
                .andExpect(status().isNoContent());

        verify(pagamentoService)
                .confirmarPagamentoPorReserva("reserva-001");
    }

    @Test
    void deveConfirmarPagamentoViaLink() throws Exception {

        mockMvc.perform(get("/pagamentos/pagar/pag-001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Pagamento confirmado! Obrigado, sua reserva está garantida."
                ));

        verify(pagamentoService)
                .confirmarPagamento("pag-001");
    }

    @Test
    void deveSimularReserva() throws Exception {

        ReservaEventoDTO dto = new ReservaEventoDTO();
        dto.setReservaId("reserva-001");

        mockMvc.perform(
                        post("/pagamentos/teste/simular-reserva")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva simulada com sucesso!"));

        verify(pagamentoService)
                .processarReserva(any());
    }
}
