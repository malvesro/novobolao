package com.opendev.bolao.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.opendev.bolao.util.DadosClassificacao;

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

    @Test
    @DisplayName("compareTo deve desempatar por acertos totais quando pontos forem iguais")
    void deveDesempatarPorAcertosTotais() throws Exception {
        Participante alice = criarParticipanteComPontuacao("Alice Silva", 10, 3, 1);
        Participante bruno = criarParticipanteComPontuacao("Bruno Souza", 10, 2, 99);

        assertThat(alice.compareTo(bruno)).isLessThan(0);
    }

    @Test
    @DisplayName("compareTo deve desempatar por acertos parciais com bonus quando pontos e acertos totais forem iguais")
    void deveDesempatarPorAcertosParciaisComBonus() throws Exception {
        Participante alice = criarParticipanteComPontuacao("Alice Silva", 10, 2, 5);
        Participante bruno = criarParticipanteComPontuacao("Bruno Souza", 10, 2, 1);

        assertThat(alice.compareTo(bruno)).isLessThan(0);
    }

    @Test
    @DisplayName("compareTo deve desempatar alfabeticamente quando demais criterios forem iguais")
    void deveDesempatarPorNomeQuandoDemaisCriteriosIguais() throws Exception {
        Participante alice = criarParticipanteComPontuacao("Alice Silva", 10, 2, 3);
        Participante bruno = criarParticipanteComPontuacao("Bruno Souza", 10, 2, 3);

        assertThat(alice.compareTo(bruno)).isLessThan(0);
    }

    private Participante criarParticipanteComPontuacao(
            String nome,
            int pontos,
            int acertosTotais,
            int acertosParciaisComBonus) throws Exception {
        Participante participante = new Participante();
        participante.setNome(nome);

        DadosClassificacao dados = new DadosClassificacao();
        dados.setPontuacao(pontos);
        dados.setQuantidadeDeAcertosTotais(acertosTotais);
        dados.setQuantidadeDeAcertosParciaisComBonus(acertosParciaisComBonus);

        Field campoPontuacaoTotal = Participante.class.getDeclaredField("pontuacaoTotal");
        campoPontuacaoTotal.setAccessible(true);
        campoPontuacaoTotal.set(participante, dados);

        return participante;
    }
}
