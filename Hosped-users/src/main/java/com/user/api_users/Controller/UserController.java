package com.user.api_users.Controller;

import com.user.api_users.DTO.AlterarSenhaRequest;
import com.user.api_users.DTO.LoginRequest;
import com.user.api_users.DTO.LoginResponse;
import com.user.api_users.model.Users;
import com.user.api_users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

        @Autowired
        private UserService userService;

        // Livre — qualquer um pode fazer login
        @PostMapping("/auth/login")
        public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
            return ResponseEntity.ok(userService.login(request));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @PostMapping("/users")
        public ResponseEntity<Users> cadastrar(@RequestBody Users user) {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.cadastrar(user));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @GetMapping("/users")
        public ResponseEntity<List<Users>> listarTodos() {
            return ResponseEntity.ok(userService.listarTodos());
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @GetMapping("/users/{id}")
        public ResponseEntity<Users> buscarPorId(@PathVariable UUID id) {
            return ResponseEntity.ok(userService.buscarPorId(id));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @PatchMapping("/users/{id}/senha")
        public ResponseEntity<Users> alterarSenha(@PathVariable UUID id,
                                                  @RequestBody AlterarSenhaRequest request) {
            return ResponseEntity.ok(userService.alterarSenha(id, request));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @DeleteMapping("/users/{id}")
        public ResponseEntity<Void> excluir(@PathVariable UUID id) {
            userService.excluir(id);
            return ResponseEntity.noContent().build();
        }
}
