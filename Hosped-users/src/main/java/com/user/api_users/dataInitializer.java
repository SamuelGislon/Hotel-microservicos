package com.user.api_users;

import com.user.api_users.model.Cargos;
import com.user.api_users.model.Users;
import com.user.api_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class dataInitializer implements CommandLineRunner{
    @Autowired
    private UserRepository repository;

    @Override
    public void run(String... args) {
        if (!repository.existsByCpf("12345678900")) {
            Users admin = new Users(
                    "Administrador",
                    Cargos.ADMINISTRADOR,
                    "12345678900",
                    BCrypt.hashpw("Admin@123", BCrypt.gensalt())
            );
            repository.save(admin);
            System.out.println("Administrador padrão criado com sucesso!");
        }
    }
}
