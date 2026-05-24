package com.opendev.bolao.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidacaoUtilsTest {

    @Test
    @DisplayName("Senhas entre 8 e 64 caracteres, com simbolos, devem ser aceitas")
    void deveAceitarSenhasComSimbolos() {
        assertThat(ValidacaoUtils.isSenhaValida("Abcdef!1")).isTrue();

        String sessentaQuatroCaracteres = "Aa1!" + "x".repeat(60);
        assertThat(sessentaQuatroCaracteres).hasSize(64);
        assertThat(ValidacaoUtils.isSenhaValida(sessentaQuatroCaracteres)).isTrue();
    }

    @Test
    @DisplayName("Senhas com tamanho invalido devem ser rejeitadas")
    void deveRejeitarSenhasComTamanhoInvalido() {
        assertThat(ValidacaoUtils.isSenhaValida("A!b1c2")).isFalse();
        assertThat(ValidacaoUtils.isSenhaValida("A".repeat(65))).isFalse();
    }

    @Test
    @DisplayName("Senhas com caracteres de controle devem ser rejeitadas")
    void deveRejeitarSenhasComControle() {
        assertThat(ValidacaoUtils.isSenhaValida("SenhaValida\u0007")).isFalse();
        assertThat(ValidacaoUtils.isSenhaValida("Senha\nInvalida")).isFalse();
    }

    @Test
    @DisplayName("Senhas com espaços nas extremidades devem ser normalizadas")
    void deveAceitarSenhasComEspacosNasExtremidades() {
        assertThat(ValidacaoUtils.isSenhaValida("  Abcdef!1  ")).isTrue();
    }
}
