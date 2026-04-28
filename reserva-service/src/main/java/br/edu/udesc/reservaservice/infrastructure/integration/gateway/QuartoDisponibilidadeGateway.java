package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.time.LocalDate;
import java.util.UUID;

public interface QuartoDisponibilidadeGateway {

    DisponibilidadeQuarto verificarDisponibilidade(UUID quartoId, LocalDate checkIn, LocalDate checkOut);

    DisponibilidadeQuarto verificarDisponibilidadePorServico(Long quartoServicoId, LocalDate checkIn, LocalDate checkOut);

    void registrarCheckOut(Long quartoServicoId);
}
