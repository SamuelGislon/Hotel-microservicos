package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.Reserva;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    @Override
    @EntityGraph(attributePaths = "hospede")
    List<Reserva> findAll();

    @EntityGraph(attributePaths = "hospede")
    List<Reserva> findByHospedeIdOrderByCriadoAtDesc(UUID hospedeId);

    @Override
    @EntityGraph(attributePaths = "hospede")
    Optional<Reserva> findById(UUID id);

    boolean existsByHospedeId(UUID hospedeId);
}
