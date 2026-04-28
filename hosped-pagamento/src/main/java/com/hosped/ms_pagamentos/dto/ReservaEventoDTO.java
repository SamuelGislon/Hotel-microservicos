package com.hosped.ms_pagamentos.dto;

import com.hosped.ms_pagamentos.model.MetodoPagamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaEventoDTO {

    private String reservaId;
    private String nomeHospede;
    private String emailHospede;
    private BigDecimal valorDiaria;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private MetodoPagamento metodoPagamento;
}