package com.user.api_users.service;
import com.user.api_users.DTO.AlterarSenhaRequest;
import com.user.api_users.DTO.LoginRequest;
import com.user.api_users.DTO.LoginResponse;
import com.user.api_users.security.JwtUtil;
import com.user.api_users.model.Users;
import com.user.api_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
                .orElseThrow(() -> new RuntimeException("CPF ou senha inválidos"));

        if (!BCrypt.checkpw(request.getSenha(), user.getSenha())) {
            throw new RuntimeException("CPF ou senha inválidos");
        }

        String token = jwtUtil.gerar(user.getCpf(), user.getNome(), user.getCargo().name());
        return new LoginResponse(token);
    }

        public Users cadastrar(Users user) {
            if (repository.existsByCpf(user.getCpf())) {
                throw new RuntimeException("CPF já cadastrado");
            }
            validarSenhaForte(user.getSenha());
            user.setSenha(BCrypt.hashpw(user.getSenha(), BCrypt.gensalt()));
            return repository.save(user);
        }

        public List<Users> listarTodos() {
            return repository.findAll();
        }

        public Users buscarPorId(UUID id) {
            return repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        }

        public Users alterarSenha(UUID id, AlterarSenhaRequest request) {
            Users user = buscarPorId(id);
            if (!BCrypt.checkpw(request.getSenhaAtual(), user.getSenha())) {
                throw new RuntimeException("Senha atual incorreta");
            }
            validarSenhaForte(request.getNovaSenha());
            user.setSenha(BCrypt.hashpw(request.getNovaSenha(), BCrypt.gensalt()));
            return repository.save(user);
        }

        public void excluir(UUID id) {
            if (!repository.existsById(id)) {
                throw new RuntimeException("Usuário não encontrado");
            }
            repository.deleteById(id);
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
}
