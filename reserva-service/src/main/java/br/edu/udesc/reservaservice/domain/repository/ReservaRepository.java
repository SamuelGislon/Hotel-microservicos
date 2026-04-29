package br.edu.udesc.reservaservice.domain.repository;

import br.edu.udesc.reservaservice.domain.model.Reserva;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        select case when count(r) > 0 then true else false end
        from Reserva r
        where r.quartoServicoId = :quartoServicoId
          and r.reservaStatus in :statuses
          and r.checkInData < :checkOutData
          and r.checkOutData > :checkInData
    """)
    boolean existsConflitoPeriodo(
        @Param("quartoServicoId") Long quartoServicoId,
        @Param("checkInData") LocalDate checkInData,
        @Param("checkOutData") LocalDate checkOutData,
        @Param("statuses") List<ReservaStatus> statuses
    );
}
