package com.user.api_users.service;
import com.user.api_users.DTO.AlterarSenhaRequest;
import com.user.api_users.DTO.CadastrarFuncionarioRequest;
import com.user.api_users.DTO.LoginRequest;
import com.user.api_users.DTO.LoginResponse;
import com.user.api_users.DTO.UserResponse;
import com.user.api_users.exception.AutenticacaoException;
import com.user.api_users.exception.CpfDuplicadoException;
import com.user.api_users.exception.RecursoNaoEncontradoException;
import com.user.api_users.security.JwtUtil;
import com.user.api_users.model.Users;
import com.user.api_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

        @Autowired
        private UserRepository repository;

        @Autowired
        private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Users user = repository.findByCpf(request.getCpf())
                .orElseThrow(() -> new AutenticacaoException("CPF ou senha inválidos"));

        if (!BCrypt.checkpw(request.getSenha(), user.getSenha())) {
            throw new AutenticacaoException("CPF ou senha inválidos");
        }

        String token = jwtUtil.gerar(user.getCpf(), user.getNome(), user.getCargo().name());
        return new LoginResponse(token);
    }

        public UserResponse cadastrar(CadastrarFuncionarioRequest request) {
            Users user = new Users(request.nome(), request.cargo(), request.cpf(), request.senha());
            return toResponse(cadastrar(user));
        }

        public Users cadastrar(Users user) {
            if (repository.existsByCpf(user.getCpf())) {
                throw new CpfDuplicadoException(user.getCpf());
            }
            validarSenhaForte(user.getSenha());
            user.setSenha(BCrypt.hashpw(user.getSenha(), BCrypt.gensalt()));
            return repository.save(user);
        }

        public List<UserResponse> listarTodos() {
            return repository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        public UserResponse buscarPorId(UUID id) {
            return toResponse(buscarEntidadePorId(id));
        }

        public Users buscarEntidadePorId(UUID id) {
            return repository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        }

        public UserResponse alterarSenha(UUID id, AlterarSenhaRequest request, Authentication authentication) {
            Users user = buscarEntidadePorId(id);
            validarPermissaoAlteracaoSenha(user, authentication);
            return toResponse(alterarSenhaInterno(user, request));
        }

        public Users alterarSenha(UUID id, AlterarSenhaRequest request) {
            Users user = buscarEntidadePorId(id);
            return alterarSenhaInterno(user, request);
        }

        private Users alterarSenhaInterno(Users user, AlterarSenhaRequest request) {
            if (!BCrypt.checkpw(request.getSenhaAtual(), user.getSenha())) {
                throw new IllegalArgumentException("Senha atual incorreta");
            }
            validarSenhaForte(request.getNovaSenha());
            user.setSenha(BCrypt.hashpw(request.getNovaSenha(), BCrypt.gensalt()));
            return repository.save(user);
        }

        public void excluir(UUID id) {
            if (!repository.existsById(id)) {
                throw new RecursoNaoEncontradoException("Usuário não encontrado");
            }
            repository.deleteById(id);
        }

        private void validarPermissaoAlteracaoSenha(Users user, Authentication authentication) {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("Usuário não autenticado");
            }
            boolean administrador = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMINISTRADOR".equals(authority.getAuthority()));
            boolean proprioUsuario = user.getCpf().equals(authentication.getName());
            if (!administrador && !proprioUsuario) {
                throw new AccessDeniedException("Funcionário só pode alterar a própria senha");
            }
        }

        private void validarSenhaForte(String senha) {
            if (senha == null || senha.length() < 8)
                throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres");
            if (!senha.matches(".*[A-Z].*"))
                throw new IllegalArgumentException("A senha deve ter pelo menos uma letra maiúscula");
            if (!senha.matches(".*[a-z].*"))
                throw new IllegalArgumentException("A senha deve ter pelo menos uma letra minúscula");
            if (!senha.matches(".*[0-9].*"))
                throw new IllegalArgumentException("A senha deve ter pelo menos um número");
            if (!senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
                throw new IllegalArgumentException("A senha deve ter pelo menos um caractere especial");
        }

        private UserResponse toResponse(Users user) {
            return new UserResponse(user.getId(), user.getNome(), user.getCpf(), user.getCargo());
        }
}
