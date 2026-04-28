package br.edu.udesc.reservaservice.shared;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.udesc.reservaservice.shared.util.CpfUtils;
import org.junit.jupiter.api.Test;

class CpfUtilsTest {

    @Test
    void deveNormalizarCpfComPontuacao() {
        assertThat(CpfUtils.normalizar("123.456.789-09")).isEqualTo("12345678909");
    }

    @Test
    void deveValidarFormatoComOnzeDigitos() {
        assertThat(CpfUtils.formatoValido("123.456.789-09")).isTrue();
        assertThat(CpfUtils.formatoValido("12345")).isFalse();
    }
}
