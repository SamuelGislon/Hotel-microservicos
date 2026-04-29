package com.user.api_users.Controller;

import com.user.api_users.DTO.AlterarSenhaRequest;
import com.user.api_users.DTO.CadastrarFuncionarioRequest;
import com.user.api_users.DTO.LoginRequest;
import com.user.api_users.DTO.LoginResponse;
import com.user.api_users.DTO.UserResponse;
import com.user.api_users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
            return ResponseEntity.ok(userService.login(request));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @PostMapping("/users")
        public ResponseEntity<UserResponse> cadastrar(@Valid @RequestBody CadastrarFuncionarioRequest user) {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.cadastrar(user));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @GetMapping("/users")
        public ResponseEntity<List<UserResponse>> listarTodos() {
            return ResponseEntity.ok(userService.listarTodos());
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @GetMapping("/users/{id}")
        public ResponseEntity<UserResponse> buscarPorId(@PathVariable UUID id) {
            return ResponseEntity.ok(userService.buscarPorId(id));
        }

        // ADMINISTRADOR altera qualquer usuário; FUNCIONARIO altera somente a própria senha.
        @PreAuthorize("isAuthenticated()")
        @PatchMapping("/users/{id}/senha")
        public ResponseEntity<UserResponse> alterarSenha(
                @PathVariable UUID id,
                @Valid @RequestBody AlterarSenhaRequest request,
                Authentication authentication
        ) {
            return ResponseEntity.ok(userService.alterarSenha(id, request, authentication));
        }

        // Apenas ADMINISTRADOR
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        @DeleteMapping("/users/{id}")
        public ResponseEntity<Void> excluir(@PathVariable UUID id) {
            userService.excluir(id);
            return ResponseEntity.noContent().build();
        }
}
