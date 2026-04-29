package com.quartos.api_quartos;
import com.quartos.api_quartos.Service.QuartoService;
import com.quartos.api_quartos.model.ItemQuarto;
import com.quartos.api_quartos.model.Quarto;
import com.quartos.api_quartos.model.StatusQuarto;
import com.quartos.api_quartos.model.TipoQuarto;
import com.quartos.api_quartos.repository.QuartoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuartoServiceTest {
    @Mock
    private QuartoRepository quartoRepository;

    @InjectMocks
    private QuartoService quartoService;

    private Quarto quarto;

    @BeforeEach
    void setUp() {
        quarto = new Quarto(1, TipoQuarto.SIMPLES, 1);
        quarto.setId(1L);
        quarto.setStatus(StatusQuarto.DISPONIVEL);
    }

    @Test
    void cadastrarQuarto() {
        when(quartoRepository.existsByNumeroQuarto(1)).thenReturn(false);
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Quarto resultado = quartoService.cadastrar(quarto);

        assertEquals(StatusQuarto.DISPONIVEL, resultado.getStatus());
        verify(quartoRepository).save(quarto);
    }

    @Test
    void cadastrarNumeroQuartoRepetido() {
        when(quartoRepository.existsByNumeroQuarto(1)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> quartoService.cadastrar(quarto));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void excluirQuartoDisponivel() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        quartoService.excluir(1L);

        verify(quartoRepository).deleteById(1L);
    }

    @Test
    void excluirQuartoOcupado() {
        quarto.setStatus(StatusQuarto.OCUPADO);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        assertThrows(RuntimeException.class, () -> quartoService.excluir(1L));
        verify(quartoRepository, never()).deleteById(any());
    }

    @Test
    void adicionarItem() {
        ItemQuarto item = new ItemQuarto("Toalha", 2);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Quarto resultado = quartoService.adicionarItem(1L, item);

        assertEquals(1, resultado.getItens().size());
        verify(quartoRepository).save(quarto);
    }

    @Test
    void adicionarItemQtdZero() {
        ItemQuarto item = new ItemQuarto("Toalha", 0);

        assertThrows(RuntimeException.class, () -> quartoService.adicionarItem(1L, item));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void checkoutAguardandoLimpeza() {
        quarto.setStatus(StatusQuarto.OCUPADO);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Quarto resultado = quartoService.checkout(1L);

        assertEquals(StatusQuarto.AGUARDANDO_LIMPEZA, resultado.getStatus());
    }

    @Test
    void checkoutBloqueiaQuartoNaoOcupado() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        assertThrows(RuntimeException.class, () -> quartoService.checkout(1L));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void marcarComoAguardandoLimpezaPorCheckOut() {
        quarto.setStatus(StatusQuarto.OCUPADO);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Quarto resultado = quartoService.marcarComoAguardandoLimpezaPorCheckOut(1L);

        assertEquals(StatusQuarto.AGUARDANDO_LIMPEZA, resultado.getStatus());
        verify(quartoRepository).save(quarto);
    }

    @Test
    void marcarComoAguardandoLimpezaPorCheckOutBloqueiaStatusInvalido() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        assertThrows(RuntimeException.class, () -> quartoService.marcarComoAguardandoLimpezaPorCheckOut(1L));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void finalizarLimpeza() {
        quarto.setStatus(StatusQuarto.AGUARDANDO_LIMPEZA);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Quarto resultado = quartoService.finalizarLimpeza(1L);

        assertEquals(StatusQuarto.DISPONIVEL, resultado.getStatus());
    }

    @Test
    void finalizarLimpezaBloqueiaStatusInvalido() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        assertThrows(RuntimeException.class, () -> quartoService.finalizarLimpeza(1L));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void marcarComoOcupadoPorCheckIn() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        Optional<Quarto> resultado = quartoService.marcarComoOcupadoPorCheckIn(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusQuarto.OCUPADO, resultado.get().getStatus());
        verify(quartoRepository).save(quarto);
    }

    @Test
    void marcarComoOcupadoPorCheckInQuartoInexistente() {
        when(quartoRepository.findById(5L)).thenReturn(Optional.empty());

        Optional<Quarto> resultado = quartoService.marcarComoOcupadoPorCheckIn(5L);

        assertTrue(resultado.isEmpty());
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void buscaPorId(){
     when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

     Quarto resultado = quartoService.buscarPorId(1L);

     assertNotNull(resultado);
     assertEquals(1L, resultado.getId());
     assertEquals(1, resultado.getNumeroQuarto());
    }

    @Test
    void buscaPorIdInexistente(){
        when(quartoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> quartoService.buscarPorId(5L));
    }

    @Test
    void listarTodosQuartos() {
        when(quartoRepository.findAll()).thenReturn(List.of(quarto));

        List<Quarto> resultado = quartoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

}
