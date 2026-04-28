package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.EventoPublicacaoLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoPublicacaoLogRepository extends JpaRepository<EventoPublicacaoLog, UUID> {
}
