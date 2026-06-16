package com.opendev.bolao.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Time;
import java.sql.Date;
import java.time.LocalTime;
import java.time.ZonedDateTime;

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
    void deveConsiderarJogoOcorridoAPartirDoHorarioDeInicio() {
        ZonedDateTime passado = ZonedDateTime.now(BolaoTime.getZoneId()).minusMinutes(1);
        Jogo jogoConcluido = criarJogo(passado);

        assertThat(jogoConcluido.jaOcorreu()).isTrue();

        ZonedDateTime futuro = ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30);
        Jogo jogoAindaNaoIniciado = criarJogo(futuro);

        assertThat(jogoAindaNaoIniciado.jaOcorreu()).isFalse();
    }

    @Test
    void devePreservarDiaCivilQuandoDataForSqlDate() {
        Jogo jogo = new Jogo();
        jogo.setData(Date.valueOf("2026-06-16"));
        jogo.setHora(Time.valueOf("22:00:00"));

        ZonedDateTime dataHora = jogo.getDataHora();

        assertThat(dataHora).isNotNull();
        assertThat(dataHora.getZone()).isEqualTo(BolaoTime.getZoneId());
        assertThat(dataHora.toLocalDate().toString()).isEqualTo("2026-06-16");
        assertThat(dataHora.toLocalTime().toString()).isEqualTo("22:00");
    }

    @Test
    void deveNormalizarHoraQuandoToLocalTimeDivergirDoEpochNoTimezoneCanonico() {
        long millisHora22 = ZonedDateTime.of(1970, 1, 1, 22, 0, 0, 0, BolaoTime.getZoneId())
                .toInstant()
                .toEpochMilli();
        Time horaDivergente = new Time(millisHora22) {
            @Override
            public LocalTime toLocalTime() {
                return LocalTime.of(1, 0);
            }
        };

        Jogo jogo = new Jogo();
        jogo.setData(Date.valueOf("2026-06-16"));
        jogo.setHora(horaDivergente);

        ZonedDateTime dataHora = jogo.getDataHora();

        assertThat(dataHora.toLocalTime()).isEqualTo(LocalTime.of(22, 0));
    }

    @Test
    void deveExporPodeAtualizarResultadoAPartirDoInicioDoJogo() {
        Jogo jogoJaIniciado = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).minusMinutes(1));
        Jogo jogoFuturo = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30));

        assertThat(jogoJaIniciado.getPodeAtualizarResultado()).isTrue();
        assertThat(jogoFuturo.getPodeAtualizarResultado()).isFalse();
    }

    private Jogo criarJogo(ZonedDateTime dataHora) {
        Jogo jogo = new Jogo();
        jogo.setData(java.util.Date.from(dataHora.toInstant()));
        jogo.setHora(Time.valueOf(dataHora.toLocalTime()));
        jogo.setGolsEquipe1(0);
        jogo.setGolsEquipe2(0);
        return jogo;
    }
}
