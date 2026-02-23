package com.opendev.bolao.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParticipanteTest {

    @Test
    @DisplayName("Setters devem sanitizar login, nome e email")
    void deveSanitizarCamposDeTexto() {
        Participante participante = new Participante();
        participante.setLogin(" <b>usuario</b> ");
        participante.setNome("Nome <i>Exemplo</i>");
        participante.setEmail("email<script>@dominio.com");

        assertThat(participante.getLogin()).isEqualTo("usuario");
        assertThat(participante.getNome()).isEqualTo("Nome Exemplo");
        assertThat(participante.getEmail()).isEqualTo("email@dominio.com");
    }

    @Test
    @DisplayName("Flags de markup devem ser ativadas quando houver HTML")
    void deveMarcarCamposComMarkup() {
        Participante participante = new Participante();
        participante.setLogin("<b>usuario</b>");
        participante.setNome("Nome");
        participante.setEmail("email@dominio.com");

        assertThat(participante.isLoginPossuiMarkup()).isTrue();
        assertThat(participante.isNomePossuiMarkup()).isFalse();
        assertThat(participante.isEmailPossuiMarkup()).isFalse();
    }
}
