package com.hosped.ms_pagamentos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoExpiracaoScheduler {

    private final PagamentoService pagamentoService;

    @Scheduled(fixedDelayString = "${pagamento.expiracao.verificacao-ms:60000}")
    public void expirarPagamentosVencidos() {
        int expirados = pagamentoService.expirarPagamentosVencidos();
        if (expirados > 0) {
            log.info("Pagamentos expirados automaticamente: {}", expirados);
        }
    }
}
