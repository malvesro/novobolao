package com.opendev.bolao.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.opendev.bolao.util.BolaoTime;

class JogoTest {

    @Test
    void deveCombinarDataEHoraEmZonaBrt() {
        ZonedDateTime esperado = ZonedDateTime.of(2026, 6, 11, 16, 0, 0, 0, BolaoTime.getZoneId());
        Jogo jogo = criarJogo(esperado);

        ZonedDateTime resultado = jogo.getDataHora();

        assertThat(resultado).isNotNull();
        assertThat(resultado.getZone()).isEqualTo(BolaoTime.getZoneId());
        assertThat(resultado.toLocalDateTime()).isEqualTo(esperado.toLocalDateTime());
    }

    @Test
    void devePermitirPalpiteAteUmaHoraAntesDoJogo() {
        ZonedDateTime inicio = ZonedDateTime.now(BolaoTime.getZoneId()).plusHours(5);
        Jogo jogo = criarJogo(inicio);

        assertThat(jogo.getPodeDarPalpite()).isTrue();

        ZonedDateTime janelaEncerrada = ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30);
        Jogo jogoEncerrado = criarJogo(janelaEncerrada);

        assertThat(jogoEncerrado.getPodeDarPalpite()).isFalse();
    }

    @Test
    void deveConsiderarJogoEncerradoAposDuasHorasDoInicio() {
        ZonedDateTime passado = ZonedDateTime.now(BolaoTime.getZoneId()).minusHours(3);
        Jogo jogoConcluido = criarJogo(passado);

        assertThat(jogoConcluido.jaOcorreu()).isTrue();

        ZonedDateTime emAndamento = ZonedDateTime.now(BolaoTime.getZoneId()).minusMinutes(30);
        Jogo jogoEmAndamento = criarJogo(emAndamento);

        assertThat(jogoEmAndamento.jaOcorreu()).isFalse();
    }

    private Jogo criarJogo(ZonedDateTime dataHora) {
        Jogo jogo = new Jogo();
        jogo.setData(Date.from(dataHora.toInstant()));
        jogo.setHora(Time.valueOf(dataHora.toLocalTime()));
        jogo.setGolsEquipe1(0);
        jogo.setGolsEquipe2(0);
        return jogo;
    }
}
