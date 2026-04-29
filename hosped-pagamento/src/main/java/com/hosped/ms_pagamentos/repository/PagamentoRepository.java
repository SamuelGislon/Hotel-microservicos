package com.hosped.ms_pagamentos.repository;

import com.hosped.ms_pagamentos.model.Pagamento;
import com.hosped.ms_pagamentos.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, String> {

    List<Pagamento> findByReservaId(String reservaId);

    List<Pagamento> findByReservaIdAndStatus(String reservaId, StatusPagamento status);

    List<Pagamento> findByStatusAndDataExpiracaoBefore(StatusPagamento status, LocalDateTime dataExpiracao);
}
