package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.Hospede;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospedeRepository extends JpaRepository<Hospede, UUID> {

    Optional<Hospede> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
