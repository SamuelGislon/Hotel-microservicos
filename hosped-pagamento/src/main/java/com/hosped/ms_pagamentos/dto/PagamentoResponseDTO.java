package com.hosped.ms_pagamentos.dto;

import com.hosped.ms_pagamentos.model.MetodoPagamento;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagamentoResponseDTO {

    private String id;
    private String reservaId;
    private BigDecimal valor;
    private StatusPagamento status;
    private MetodoPagamento metodoPagamento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataExpiracao;
    private String nomeHospede;
}