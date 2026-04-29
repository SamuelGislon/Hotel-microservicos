package com.quartos.api_quartos.Service;

import com.quartos.api_quartos.model.ItemQuarto;
import com.quartos.api_quartos.model.Quarto;
import com.quartos.api_quartos.model.StatusQuarto;
import com.quartos.api_quartos.repository.QuartoRepository;
import com.quartos.api_quartos.exception.ConflitoException;
import com.quartos.api_quartos.exception.RecursoNaoEncontradoException;
import com.quartos.api_quartos.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuartoService {

    @Autowired
    private QuartoRepository quartoRepository;

    public Quarto cadastrar (Quarto quarto){
        if(quartoRepository.existsByNumeroQuarto(quarto.getNumeroQuarto())){
            throw new ConflitoException("Já existe um quarto com esse número");
        }
        quarto.setStatus(StatusQuarto.DISPONIVEL);
        return quartoRepository.save(quarto);
    }
    public List<Quarto> listarTodos() {
        return quartoRepository.findAll();
    }

    public Quarto buscarPorId(Long id) {
        return quartoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado"));
    }
    public Quarto atualizar(Long id, Quarto quartoAtualizado){
        Quarto quarto = buscarPorId(id);
        quarto.setNumeroQuarto(quartoAtualizado.getNumeroQuarto());
        quarto.setTipo(quartoAtualizado.getTipo());
        quarto.setCapacidade(quartoAtualizado.getCapacidade());
        return quartoRepository.save(quarto);
    }
    public void excluir(Long id) {
        Quarto quarto = buscarPorId(id);
        if (quarto.getStatus() == StatusQuarto.OCUPADO) {
            throw new ConflitoException("Não é possível excluir um quarto ocupado");
        }
        quartoRepository.deleteById(id);
    }
    public Quarto adicionarItem(Long id, ItemQuarto item) {
        if (item.getQuantidade() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }
        if (item.getNomeItem() == null || item.getNomeItem().isBlank()) {
            throw new RegraNegocioException("Nome do item é obrigatório");
        }
        Quarto quarto = buscarPorId(id);

        quarto.getItens().stream()
                .filter(i -> i.getNomeItem().equalsIgnoreCase(item.getNomeItem()))
                .findFirst()
                .ifPresentOrElse(
                        itemExistente -> itemExistente.setQuantidade(item.getQuantidade()),
                        () -> quarto.addItem(item)
                );

        return quartoRepository.save(quarto);
    }
    public Quarto removerItem(Long quartoId, Long itemId) {
        Quarto quarto = buscarPorId(quartoId);
        quarto.getItens().stream()
                .filter(i -> i.getIdItem().equals(itemId))
                .findFirst()
                .ifPresent(quarto::removeItem);
        return quartoRepository.save(quarto);
    }
    public List<Quarto> listarAguardandoLimpeza() {
        return quartoRepository.findByStatus(StatusQuarto.AGUARDANDO_LIMPEZA);
    }
    public Quarto checkout(Long id) {
        return marcarComoAguardandoLimpezaPorCheckOut(id);
    }
    public Quarto finalizarLimpeza(Long id) {
        Quarto quarto = buscarPorId(id);
        if (quarto.getStatus() != StatusQuarto.AGUARDANDO_LIMPEZA) {
            throw new ConflitoException("Finalização de limpeza só é permitida para quarto aguardando limpeza");
        }
        quarto.setStatus(StatusQuarto.DISPONIVEL);
        return quartoRepository.save(quarto);
    }

    @Transactional
    public Optional<Quarto> marcarComoOcupadoPorCheckIn(Long id) {
        return quartoRepository.findById(id)
                .map(quarto -> {
                    quarto.setStatus(StatusQuarto.OCUPADO);
                    return quartoRepository.save(quarto);
                });
    }

    @Transactional
    public Quarto marcarComoAguardandoLimpezaPorCheckOut(Long id) {
        Quarto quarto = buscarPorId(id);
        if (quarto.getStatus() != StatusQuarto.OCUPADO) {
            throw new ConflitoException("Checkout só é permitido para quarto ocupado");
        }
        quarto.setStatus(StatusQuarto.AGUARDANDO_LIMPEZA);
        return quartoRepository.save(quarto);
    }
}
