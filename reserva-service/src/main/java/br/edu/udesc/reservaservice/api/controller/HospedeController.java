package br.edu.udesc.reservaservice.api.controller;

import br.edu.udesc.reservaservice.api.request.AtualizarHospedeRequest;
import br.edu.udesc.reservaservice.api.request.CriarHospedeRequest;
import br.edu.udesc.reservaservice.api.response.HospedeResponse;
import br.edu.udesc.reservaservice.application.dto.AtualizarHospedeCommand;
import br.edu.udesc.reservaservice.application.dto.CriarHospedeCommand;
import br.edu.udesc.reservaservice.application.mapper.RespostaApiMapper;
import br.edu.udesc.reservaservice.application.service.HospedeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hospedes")
@Tag(name = "Hóspedes", description = "Operações de cadastro, consulta, atualização e exclusão de hóspedes")
public class HospedeController {

    private final HospedeService hospedeService;
    private final RespostaApiMapper respostaApiMapper;

    @PostMapping
    @Operation(summary = "Cadastrar hóspede")
    public ResponseEntity<HospedeResponse> cadastrar(@Valid @RequestBody CriarHospedeRequest request) {
        HospedeResponse response = respostaApiMapper.toResponse(hospedeService.cadastrar(
            new CriarHospedeCommand(request.nomeCompleto(), request.cpf(), request.email(), request.telefone())
        ));
        return ResponseEntity
            .created(URI.create("/api/v1/hospedes/" + response.id()))
            .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar hóspedes")
    public ResponseEntity<List<HospedeResponse>> listar() {
        return ResponseEntity.ok(
            hospedeService.listar().stream().map(respostaApiMapper::toResponse).toList()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar hóspede por id")
    public ResponseEntity<HospedeResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(hospedeService.buscarPorId(id)));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar hóspede por CPF")
    public ResponseEntity<HospedeResponse> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(respostaApiMapper.toResponse(hospedeService.buscarPorCpf(cpf)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar hóspede")
    public ResponseEntity<HospedeResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarHospedeRequest request) {
        HospedeResponse response = respostaApiMapper.toResponse(hospedeService.atualizar(
            id,
            new AtualizarHospedeCommand(
                request.nomeCompleto(),
                request.cpf(),
                request.email(),
                request.telefone(),
                request.ativo()
            )
        ));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir hóspede")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        hospedeService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
