package com.hosped.ms_pagamentos.controller;

import com.hosped.ms_pagamentos.dto.PagamentoResponseDTO;
import com.hosped.ms_pagamentos.dto.StatusPagamentoIntegracaoDTO;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import com.hosped.ms_pagamentos.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<PagamentoResponseDTO>> buscarPorReservaId(@PathVariable String reservaId) {
        return ResponseEntity.ok(pagamentoService.buscarPorReservaId(reservaId));
    }

    @GetMapping("/reserva/{reservaId}/status-integracao")
    public ResponseEntity<StatusPagamentoIntegracaoDTO> consultarStatusIntegracao(@PathVariable String reservaId) {
        return ResponseEntity.ok(pagamentoService.consultarStatusIntegracao(reservaId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable String id, @RequestParam StatusPagamento status) {
        pagamentoService.atualizarStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserva/{reservaId}/confirmar")
    public ResponseEntity<Void> confirmarPagamentoPorReserva(@PathVariable String reservaId) {
        pagamentoService.confirmarPagamentoPorReserva(reservaId);
        return ResponseEntity.noContent().build();
    }

    // Usado pelo link de confirmação enviado por e-mail.
    @GetMapping("/pagar/{id}")
    public ResponseEntity<String> confirmarPagamento(@PathVariable String id) {
        pagamentoService.confirmarPagamento(id);
        return ResponseEntity.ok("Pagamento confirmado! Obrigado, sua reserva está garantida.");
    }

    // Atalho para testes manuais de integração.
    @PostMapping("/teste/simular-reserva")
    public ResponseEntity<String> simularReserva(@RequestBody com.hosped.ms_pagamentos.dto.ReservaEventoDTO dto) {
        pagamentoService.processarReserva(dto);
        return ResponseEntity.ok("Reserva simulada com sucesso!");
    }
}
