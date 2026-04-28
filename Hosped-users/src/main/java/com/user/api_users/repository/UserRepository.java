package com.user.api_users.repository;
import com.user.api_users.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}
