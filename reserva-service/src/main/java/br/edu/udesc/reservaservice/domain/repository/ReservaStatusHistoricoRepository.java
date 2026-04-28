package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.ReservaStatusHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaStatusHistoricoRepository extends JpaRepository<ReservaStatusHistorico, UUID> {

    List<ReservaStatusHistorico> findByReservaIdOrderByAtualizadoAtAsc(UUID reservaId);
}
