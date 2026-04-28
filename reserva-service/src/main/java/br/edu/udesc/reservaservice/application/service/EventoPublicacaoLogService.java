package br.edu.udesc.reservaservice.application.service;

import br.edu.udesc.reservaservice.domain.enums.StatusPublicacaoEvento;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEvent;
import br.edu.udesc.reservaservice.domain.model.EventoPublicacaoLog;
import br.edu.udesc.reservaservice.domain.repository.EventoPublicacaoLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoPublicacaoLogService {

    private final EventoPublicacaoLogRepository eventoPublicacaoLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSucesso(ReservaDomainEvent event, String payloadResumo) {
        EventoPublicacaoLog log = new EventoPublicacaoLog(
            event.eventType(),
            event.reservaId(),
            payloadResumo,
            StatusPublicacaoEvento.SUCESSO
        );
        eventoPublicacaoLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalha(ReservaDomainEvent event, String payloadResumo, String mensagemErro) {
        EventoPublicacaoLog log = new EventoPublicacaoLog(
            event.eventType(),
            event.reservaId(),
            payloadResumo,
            StatusPublicacaoEvento.FALHA
        );
        log.marcarFalha(mensagemErro);
        eventoPublicacaoLogRepository.save(log);
    }
}
