package com.quartos.api_quartos.repository;

import com.quartos.api_quartos.model.Quarto;
import com.quartos.api_quartos.model.StatusQuarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    boolean existsByNumeroQuarto(int numeroQuarto);

    List<Quarto> findByStatus(StatusQuarto status);
}
