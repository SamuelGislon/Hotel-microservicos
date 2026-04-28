package com.user.api_users;

import com.user.api_users.model.Cargos;
import com.user.api_users.model.Users;
import com.user.api_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class dataInitializer implements CommandLineRunner{
    @Autowired
    private UserRepository repository;

    @Value("${users.seed-admin.enabled:false}")
    private boolean seedAdminEnabled;

    @Value("${users.seed-admin.cpf:00000000000}")
    private String adminCpf;

    @Value("${users.seed-admin.password:Admin@123}")
    private String adminPassword;

    @Value("${users.seed-admin.name:Administrador Local}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (!seedAdminEnabled) {
            return;
        }

        if (!repository.existsByCpf(adminCpf)) {
            Users admin = new Users(
                    adminName,
                    Cargos.ADMINISTRADOR,
                    adminCpf,
                    BCrypt.hashpw(adminPassword, BCrypt.gensalt())
            );
            repository.save(admin);
            System.out.println("Administrador local criado com sucesso!");
        }
    }
}
