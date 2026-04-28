package br.edu.udesc.reservaservice.api.controller;

import br.edu.udesc.reservaservice.api.request.AlternarIndisponibilidadeRequest;
import br.edu.udesc.reservaservice.api.response.DisponibilidadeQuartoResponse;
import br.edu.udesc.reservaservice.api.response.OperacaoTecnicaResponse;
import br.edu.udesc.reservaservice.api.response.StatusPagamentoIntegracaoResponse;
import br.edu.udesc.reservaservice.application.mapper.RespostaApiMapper;
import br.edu.udesc.reservaservice.application.service.TecnicoService;
import br.edu.udesc.reservaservice.infrastructure.integration.client.SimuladorServicosExternosState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tecnico")
@Tag(name = "Técnico", description = "Endpoints de health, simulação de integrações e demonstração de Circuit Breaker")
public class TecnicoController {

    private final TecnicoService tecnicoService;
    private final SimuladorServicosExternosState simuladorServicosExternosState;
    private final RespostaApiMapper respostaApiMapper;

    @GetMapping("/quartos/{quartoId}/disponibilidade")
    @Operation(summary = "Consultar disponibilidade de quarto via gateway protegido")
    public ResponseEntity<DisponibilidadeQuartoResponse> consultarDisponibilidade(
        @PathVariable UUID quartoId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInData,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutData
    ) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(
            tecnicoService.consultarDisponibilidadeQuarto(quartoId, checkInData, checkOutData)
        ));
    }

    @GetMapping("/pagamentos/{reservaId}/status-integracao")
    @Operation(summary = "Consultar status de integração de pagamento via gateway protegido")
    public ResponseEntity<StatusPagamentoIntegracaoResponse> consultarStatusPagamento(@PathVariable UUID reservaId) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(tecnicoService.consultarStatusPagamento(reservaId)));
    }

    @PostMapping("/simulacoes/quarto-service/indisponivel")
    @Operation(summary = "Ativar ou desativar indisponibilidade simulada do quarto-service")
    public ResponseEntity<OperacaoTecnicaResponse> alternarIndisponibilidadeQuarto(
        @RequestBody AlternarIndisponibilidadeRequest request
    ) {
        boolean ativo = tecnicoService.alternarIndisponibilidadeQuarto(request.ativo());
        return ResponseEntity.ok(new OperacaoTecnicaResponse(
            "simulacao-quarto-service",
            "Indisponibilidade simulada do quarto-service definida como " + ativo
        ));
    }

    @PostMapping("/simulacoes/pagamento-service/indisponivel")
    @Operation(summary = "Ativar ou desativar indisponibilidade simulada do pagamento-service")
    public ResponseEntity<OperacaoTecnicaResponse> alternarIndisponibilidadePagamento(
        @RequestBody AlternarIndisponibilidadeRequest request
    ) {
        boolean ativo = tecnicoService.alternarIndisponibilidadePagamento(request.ativo());
        return ResponseEntity.ok(new OperacaoTecnicaResponse(
            "simulacao-pagamento-service",
            "Indisponibilidade simulada do pagamento-service definida como " + ativo
        ));
    }

    @PostMapping("/simulacoes/pagamento-service/reservas/{reservaId}/confirmacao")
    @Operation(summary = "Publicar evento externo simulado de pagamento confirmado")
    public ResponseEntity<OperacaoTecnicaResponse> simularEventoPagamentoConfirmado(@PathVariable UUID reservaId) {
        tecnicoService.simularEventoPagamentoConfirmado(reservaId);
        return ResponseEntity.ok(new OperacaoTecnicaResponse(
            "evento-pagamento-confirmado",
            "Evento externo de pagamento confirmado publicado para a reserva " + reservaId
        ));
    }

    @GetMapping("/simulacoes/quarto-service/disponibilidade/{quartoId}")
    @Operation(summary = "Simulador local do quarto-service")
    public ResponseEntity<DisponibilidadeQuartoResponse> simuladorQuartoService(@PathVariable UUID quartoId) {
        if (simuladorServicosExternosState.isQuartoIndisponivel()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new DisponibilidadeQuartoResponse(quartoId, false, false, "quarto-service indisponível no simulador")
            );
        }
        return ResponseEntity.ok(
            new DisponibilidadeQuartoResponse(quartoId, true, false, "quarto disponível no simulador local")
        );
    }

    @GetMapping("/simulacoes/pagamento-service/reservas/{reservaId}/status")
    @Operation(summary = "Simulador local do pagamento-service")
    public ResponseEntity<StatusPagamentoIntegracaoResponse> simuladorPagamentoService(@PathVariable UUID reservaId) {
        if (simuladorServicosExternosState.isPagamentoIndisponivel()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new StatusPagamentoIntegracaoResponse(
                    reservaId,
                    "INDISPONIVEL",
                    false,
                    "pagamento-service indisponível no simulador"
                )
            );
        }
        return ResponseEntity.ok(
            new StatusPagamentoIntegracaoResponse(
                reservaId,
                "SEM_PAGAMENTO_REAL",
                false,
                "simulação local do pagamento-service disponível"
            )
        );
    }
}
