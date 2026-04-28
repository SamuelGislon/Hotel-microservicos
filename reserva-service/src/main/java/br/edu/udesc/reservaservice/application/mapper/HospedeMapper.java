package br.edu.udesc.reservaservice.application.mapper;

import br.edu.udesc.reservaservice.application.dto.HospedeDto;
import br.edu.udesc.reservaservice.domain.model.Hospede;
import org.springframework.stereotype.Component;

@Component
public class HospedeMapper {

    public HospedeDto toDto(Hospede hospede) {
        return new HospedeDto(
            hospede.getId(),
            hospede.getNomeCompleto(),
            hospede.getCpf(),
            hospede.getEmail(),
            hospede.getTelefone(),
            hospede.isAtivo(),
            hospede.getCriadoAt(),
            hospede.getAtualizadoAt()
        );
    }
}
