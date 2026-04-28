package br.edu.udesc.reservaservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.udesc.reservaservice.application.dto.AtualizarHospedeCommand;
import br.edu.udesc.reservaservice.application.dto.CriarHospedeCommand;
import br.edu.udesc.reservaservice.application.mapper.HospedeMapper;
import br.edu.udesc.reservaservice.domain.exception.CpfDuplicadoException;
import br.edu.udesc.reservaservice.domain.exception.ExclusaoHospedeNaoPermitidaException;
import br.edu.udesc.reservaservice.domain.model.Hospede;
import br.edu.udesc.reservaservice.domain.repository.HospedeRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HospedeServiceTest {

    @Mock
    private HospedeRepository hospedeRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private HospedeService hospedeService;

    @BeforeEach
    void setUp() {
        hospedeService = new HospedeService(hospedeRepository, reservaRepository, new HospedeMapper());
    }

    @Test
    void deveImpedirCadastroDeCpfDuplicado() {
        when(hospedeRepository.existsByCpf("12345678909")).thenReturn(true);

        assertThatThrownBy(() -> hospedeService.cadastrar(
            new CriarHospedeCommand("Maria Silva", "123.456.789-09", "maria@email.com", "48999999999")
        )).isInstanceOf(CpfDuplicadoException.class);
    }

    @Test
    void deveBloquearExclusaoQuandoExistirReservaVinculada() {
        UUID hospedeId = UUID.randomUUID();
        Hospede hospede = new Hospede("Maria Silva", "12345678909", "maria@email.com", "48999999999");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(reservaRepository.existsByHospedeId(hospedeId)).thenReturn(true);

        assertThatThrownBy(() -> hospedeService.excluir(hospedeId))
            .isInstanceOf(ExclusaoHospedeNaoPermitidaException.class);

        verify(hospedeRepository, never()).delete(any());
    }

    @Test
    void deveAtualizarHospedeQuandoCpfNaoConflitar() {
        UUID hospedeId = UUID.randomUUID();
        Hospede hospede = new Hospede("Maria Silva", "12345678909", "maria@email.com", "48999999999");

        when(hospedeRepository.findById(hospedeId)).thenReturn(Optional.of(hospede));
        when(hospedeRepository.findByCpf("98765432100")).thenReturn(Optional.empty());
        when(hospedeRepository.save(any(Hospede.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hospedeService.atualizar(
            hospedeId,
            new AtualizarHospedeCommand("Maria Souza", "987.654.321-00", "maria@novo.com", "48988887777", true)
        );

        verify(hospedeRepository).save(any(Hospede.class));
    }
}
