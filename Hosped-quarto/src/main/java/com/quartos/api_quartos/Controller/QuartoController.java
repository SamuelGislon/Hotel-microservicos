package com.quartos.api_quartos.Controller;

import com.quartos.api_quartos.Service.QuartoService;
import com.quartos.api_quartos.dto.CriarQuartoRequest;
import com.quartos.api_quartos.dto.ItemQuartoRequest;
import com.quartos.api_quartos.model.ItemQuarto;
import com.quartos.api_quartos.model.Quarto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    @Autowired
    private QuartoService quartoService;

    @PostMapping
    public ResponseEntity<Quarto> cadastrar(@Valid @RequestBody CriarQuartoRequest request){
        Quarto quarto = new Quarto(request.numeroQuarto(), request.tipo(), request.capacidade());
        return ResponseEntity.status(HttpStatus.CREATED).body(quartoService.cadastrar(quarto));
    }
    @GetMapping
    public ResponseEntity<List<Quarto>> listarTodos(){
        return ResponseEntity.ok(quartoService.listarTodos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Quarto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.buscarPorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Quarto> atualizar(@PathVariable Long id, @RequestBody Quarto quarto){
        return ResponseEntity.ok(quartoService.atualizar(id, quarto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        quartoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/itens")
    public ResponseEntity<Quarto> adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemQuartoRequest request) {
        ItemQuarto item = new ItemQuarto(request.nomeItem(), request.quantidade());
        return ResponseEntity.status(HttpStatus.CREATED).body(quartoService.adicionarItem(id, item));
    }
    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        quartoService.removerItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/limpeza")
    public ResponseEntity<List<Quarto>> listarAguardandoLimpeza() {
        return ResponseEntity.ok(quartoService.listarAguardandoLimpeza());
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<Quarto> checkout(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.checkout(id));
    }
    @PatchMapping("/{id}/limpeza")
    public ResponseEntity<Quarto> finalizarLimpeza(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.finalizarLimpeza(id));
    }
}
