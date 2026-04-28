package br.edu.udesc.reservaservice.api.controller;

import br.edu.udesc.reservaservice.api.request.CriarReservaRequest;
import br.edu.udesc.reservaservice.api.request.RegistrarComentarioReservaRequest;
import br.edu.udesc.reservaservice.api.response.ReservaComentarioResponse;
import br.edu.udesc.reservaservice.api.response.ReservaResponse;
import br.edu.udesc.reservaservice.application.dto.CriarReservaCommand;
import br.edu.udesc.reservaservice.application.mapper.RespostaApiMapper;
import br.edu.udesc.reservaservice.application.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Criação, consulta e transições de status das reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final RespostaApiMapper respostaApiMapper;

    @PostMapping
    @Operation(summary = "Criar reserva")
    public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody CriarReservaRequest request) {
        ReservaResponse response = respostaApiMapper.toResponse(reservaService.criar(
            new CriarReservaCommand(
                request.hospedeId(),
                request.quartoId(),
                request.quartoServicoId(),
                request.quartoNumero(),
                request.checkInData(),
                request.checkOutData(),
                request.pagamentoModo(),
                request.valorDiaria(),
                request.metodoPagamento()
            )
        ));
        return ResponseEntity.created(URI.create("/api/v1/reservas/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar reservas")
    public ResponseEntity<List<ReservaResponse>> listar() {
        return ResponseEntity.ok(
            reservaService.listar().stream().map(respostaApiMapper::toResponse).toList()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por id")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(reservaService.buscarPorId(id)));
    }

    @GetMapping("/hospede/{hospedeId}")
    @Operation(summary = "Listar histórico de reservas por hóspede")
    public ResponseEntity<List<ReservaResponse>> listarPorHospede(@PathVariable UUID hospedeId) {
        return ResponseEntity.ok(
            reservaService.listarPorHospede(hospedeId).stream().map(respostaApiMapper::toResponse).toList()
        );
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "Realizar check-in")
    public ResponseEntity<ReservaResponse> realizarCheckIn(@PathVariable UUID id) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(reservaService.realizarCheckIn(id)));
    }

    @PostMapping("/{id}/check-out")
    @Operation(summary = "Realizar check-out")
    public ResponseEntity<ReservaResponse> realizarCheckOut(@PathVariable UUID id) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(reservaService.realizarCheckOut(id)));
    }

    @PostMapping("/{id}/confirma-pagamento")
    @Operation(summary = "Confirmar pagamento antecipado")
    public ResponseEntity<ReservaResponse> confirmarPagamento(@PathVariable UUID id) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(reservaService.confirmarPagamento(id)));
    }

    @PostMapping("/{id}/comentarios")
    @Operation(summary = "Registrar comentário de encerramento")
    public ResponseEntity<ReservaComentarioResponse> registrarComentario(
        @PathVariable UUID id,
        @Valid @RequestBody RegistrarComentarioReservaRequest request
    ) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(
            reservaService.registrarComentario(id, request.comentario())
        ));
    }
}
