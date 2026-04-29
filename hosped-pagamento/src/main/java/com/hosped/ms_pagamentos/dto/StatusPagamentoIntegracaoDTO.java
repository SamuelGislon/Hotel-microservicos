package com.hosped.ms_pagamentos.dto;

public record StatusPagamentoIntegracaoDTO(
        String reservaId,
        String statusExterno,
        boolean fallbackAcionado,
        String mensagem
) {
}
