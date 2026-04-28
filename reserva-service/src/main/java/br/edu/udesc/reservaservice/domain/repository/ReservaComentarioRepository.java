package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.ReservaComentario;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaComentarioRepository extends JpaRepository<ReservaComentario, UUID> {

    List<ReservaComentario> findByReservaIdOrderByCriadoAtAsc(UUID reservaId);
}
