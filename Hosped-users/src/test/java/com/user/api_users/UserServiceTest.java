package com.user.api_users;

import com.user.api_users.DTO.AlterarSenhaRequest;
import com.user.api_users.DTO.UserResponse;
import com.user.api_users.DTO.LoginRequest;
import com.user.api_users.DTO.LoginResponse;
import com.user.api_users.exception.AutenticacaoException;
import com.user.api_users.model.Cargos;
import com.user.api_users.model.Users;
import com.user.api_users.repository.UserRepository;
import com.user.api_users.security.JwtUtil;
import com.user.api_users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private Users admin;

    @BeforeEach
    void setUp() {
        admin = new Users(
                "Administrador",
                Cargos.ADMINISTRADOR,
                "12345678900",
                BCrypt.hashpw("Admin@123", BCrypt.gensalt())
        );
        admin.setId(UUID.randomUUID());
    }

    @Test
    void loginCredenciaisValidas() {
        LoginRequest request = new LoginRequest();
        request.setCpf("12345678900");
        request.setSenha("Admin@123");

        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(admin));
        when(jwtUtil.gerar(admin.getId(), admin.getCpf(), admin.getNome(), admin.getCargo().name()))
                .thenReturn("token-fake");

        LoginResponse resultado = userService.login(request);

        assertNotNull(resultado);
        assertEquals("token-fake", resultado.getToken());
        verify(jwtUtil).gerar(admin.getId(), admin.getCpf(), admin.getNome(), admin.getCargo().name());
    }

    @Test
    void loginCpfInexistente() {
        LoginRequest request = new LoginRequest();
        request.setCpf("00000000000");
        request.setSenha("Admin@123");

        when(repository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThrows(AutenticacaoException.class, () -> userService.login(request));
    }

    @Test
    void loginSenhaErrada() {
        LoginRequest request = new LoginRequest();
        request.setCpf("12345678900");
        request.setSenha("SenhaErrada@1");

        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(admin));

        assertThrows(AutenticacaoException.class, () -> userService.login(request));
    }

    @Test
    void cadastrarCpfNovo() {
        Users novoUser = new Users("Funcionario", Cargos.FUNCIONARIO, "98765432100", "Teste@123");

        when(repository.existsByCpf("98765432100")).thenReturn(false);
        when(repository.save(any())).thenReturn(novoUser);

        Users resultado = userService.cadastrar(novoUser);

        assertNotNull(resultado);
        verify(repository).save(any());
    }

    @Test
    void cadastrarCpfDuplicado() {
        Users novoUser = new Users("Funcionario", Cargos.FUNCIONARIO, "12345678900", "Teste@123");

        when(repository.existsByCpf("12345678900")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.cadastrar(novoUser));
        verify(repository, never()).save(any());
    }

    @Test
    void cadastrarSenhaFraca() {
        Users novoUser = new Users("Funcionario", Cargos.FUNCIONARIO, "98765432100", "123456");

        when(repository.existsByCpf("98765432100")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.cadastrar(novoUser));
        verify(repository, never()).save(any());
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(admin));

        List<UserResponse> resultado = userService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("12345678900", resultado.get(0).cpf());
    }

    @Test
    void excluirUsuarioExistente() {
        UUID id = admin.getId();
        when(repository.existsById(id)).thenReturn(true);

        userService.excluir(id);

        verify(repository).deleteById(id);
    }

    @Test
    void excluirUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.excluir(id));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void alterarSenhaSenhaAtualCorreta() {
        UUID id = admin.getId();
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual("Admin@123");
        request.setNovaSenha("NovaSenha@123");

        when(repository.findById(id)).thenReturn(Optional.of(admin));
        when(repository.save(any())).thenReturn(admin);

        Users resultado = userService.alterarSenha(id, request);

        assertNotNull(resultado);
        verify(repository).save(any());
    }

    @Test
    void alterarSenhaSenhaAtualErrada() {
        UUID id = admin.getId();
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual("SenhaErrada@1");
        request.setNovaSenha("NovaSenha@123");

        when(repository.findById(id)).thenReturn(Optional.of(admin));

        assertThrows(RuntimeException.class, () -> userService.alterarSenha(id, request));
        verify(repository, never()).save(any());
    }
}
