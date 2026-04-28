package com.hosped.ms_pagamentos.repository;

import com.hosped.ms_pagamentos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, String> {

    List<Pagamento> findByReservaId(String reservaId);
}