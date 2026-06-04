package com.opendev.bolao.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;

class OtpServiceTest {

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl();
    }

    @Test
    void deveGerarCodigoComSeisCaracteres() {
        String codigo = otpService.gerarCodigo();
        assertThat(codigo).hasSize(6);
    }

    @Test
    void deveConterDiferentesTiposDeCaracteresAoLongoDoTempo() {
        boolean encontrouGrafico = false;
        boolean encontrouNumero = false;
        boolean encontrouLetra = false;
        
        // Caracteres gráficos definidos: @#$%&*!?
        Pattern grafico = Pattern.compile("[@#$%&*!?]");
        Pattern numero = Pattern.compile("[0-9]");
        Pattern letra = Pattern.compile("[a-zA-Z]");

        for (int i = 0; i < 100; i++) {
            String codigo = otpService.gerarCodigo();
            if (grafico.matcher(codigo).find()) encontrouGrafico = true;
            if (numero.matcher(codigo).find()) encontrouNumero = true;
            if (letra.matcher(codigo).find()) encontrouLetra = true;
        }

        assertThat(encontrouGrafico).as("Deve gerar caracteres gráficos").isTrue();
        assertThat(encontrouNumero).as("Deve gerar números").isTrue();
        assertThat(encontrouLetra).as("Deve gerar letras").isTrue();
    }

    @Test
    void deveValidarCodigoCorretamente() {
        String email = "teste@exemplo.com";
        String codigo = otpService.gerarCodigo();
        
        otpService.armazenar(email, codigo);
        
        assertThat(otpService.validar(email, codigo)).isTrue();
        assertThat(otpService.validar(email, "WRONG")).isFalse();
    }

    @Test
    void deveConsumirCodigoAposUso() {
        String email = "teste@exemplo.com";
        String codigo = otpService.gerarCodigo();
        
        otpService.armazenar(email, codigo);
        assertThat(otpService.validar(email, codigo)).isTrue();
        
        otpService.consumir(email);
        assertThat(otpService.validar(email, codigo)).isFalse();
    }
}
