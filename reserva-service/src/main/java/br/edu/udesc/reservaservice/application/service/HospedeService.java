package br.edu.udesc.reservaservice.application.service;

import br.edu.udesc.reservaservice.application.dto.AtualizarHospedeCommand;
import br.edu.udesc.reservaservice.application.dto.CriarHospedeCommand;
import br.edu.udesc.reservaservice.application.dto.HospedeDto;
import br.edu.udesc.reservaservice.application.mapper.HospedeMapper;
import br.edu.udesc.reservaservice.domain.exception.CpfDuplicadoException;
import br.edu.udesc.reservaservice.domain.exception.ExclusaoHospedeNaoPermitidaException;
import br.edu.udesc.reservaservice.domain.exception.HospedeNaoEncontradoException;
import br.edu.udesc.reservaservice.domain.exception.RegraDeNegocioException;
import br.edu.udesc.reservaservice.domain.model.Hospede;
import br.edu.udesc.reservaservice.domain.repository.HospedeRepository;
import br.edu.udesc.reservaservice.domain.repository.ReservaRepository;
import br.edu.udesc.reservaservice.shared.util.CpfUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HospedeService {

    private final HospedeRepository hospedeRepository;
    private final ReservaRepository reservaRepository;
    private final HospedeMapper hospedeMapper;

    @Transactional
    public HospedeDto cadastrar(CriarHospedeCommand command) {
        validarCpf(command.cpf());
        String cpfNormalizado = CpfUtils.normalizar(command.cpf());
        if (hospedeRepository.existsByCpf(cpfNormalizado)) {
            throw new CpfDuplicadoException(cpfNormalizado);
        }

        Hospede hospede = new Hospede(command.nomeCompleto(), cpfNormalizado, command.email(), command.telefone());
        return hospedeMapper.toDto(hospedeRepository.save(hospede));
    }

    @Transactional
    public HospedeDto atualizar(UUID hospedeId, AtualizarHospedeCommand command) {
        validarCpf(command.cpf());
        Hospede hospede = hospedeRepository.findById(hospedeId)
            .orElseThrow(() -> new HospedeNaoEncontradoException(hospedeId));

        String cpfNormalizado = CpfUtils.normalizar(command.cpf());
        hospedeRepository.findByCpf(cpfNormalizado)
            .filter(outroHospede -> !outroHospede.getId().equals(hospedeId))
            .ifPresent(outroHospede -> {
                throw new CpfDuplicadoException(cpfNormalizado);
            });

        hospede.atualizar(
            command.nomeCompleto(),
            cpfNormalizado,
            command.email(),
            command.telefone(),
            command.ativo()
        );
        return hospedeMapper.toDto(hospedeRepository.save(hospede));
    }

    @Transactional(readOnly = true)
    public HospedeDto buscarPorId(UUID hospedeId) {
        return hospedeRepository.findById(hospedeId)
            .map(hospedeMapper::toDto)
            .orElseThrow(() -> new HospedeNaoEncontradoException(hospedeId));
    }

    @Transactional(readOnly = true)
    public List<HospedeDto> listar() {
        return hospedeRepository.findAll()
            .stream()
            .map(hospedeMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public HospedeDto buscarPorCpf(String cpf) {
        validarCpf(cpf);
        String cpfNormalizado = CpfUtils.normalizar(cpf);
        return hospedeRepository.findByCpf(cpfNormalizado)
            .map(hospedeMapper::toDto)
            .orElseThrow(() -> new HospedeNaoEncontradoException(cpfNormalizado));
    }

    @Transactional
    public void excluir(UUID hospedeId) {
        Hospede hospede = hospedeRepository.findById(hospedeId)
            .orElseThrow(() -> new HospedeNaoEncontradoException(hospedeId));

        if (reservaRepository.existsByHospedeId(hospedeId)) {
            throw new ExclusaoHospedeNaoPermitidaException();
        }

        hospedeRepository.delete(hospede);
    }

    private void validarCpf(String cpf) {
        if (!CpfUtils.formatoValido(cpf)) {
            throw new RegraDeNegocioException("CPF inválido. Informe 11 dígitos numéricos");
        }
    }
}
