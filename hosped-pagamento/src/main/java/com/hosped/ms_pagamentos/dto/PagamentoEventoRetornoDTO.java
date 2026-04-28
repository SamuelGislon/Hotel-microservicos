package com.hosped.ms_pagamentos.dto;

import com.hosped.ms_pagamentos.model.StatusPagamento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PagamentoEventoRetornoDTO {

    private String reservaId;
    private StatusPagamento status;
    private LocalDateTime dataAtualizacao;
}