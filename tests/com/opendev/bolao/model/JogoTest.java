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

    @Test
    void devePermitirExclusaoAdministrativaApenasParaJogoFuturoSemResultado() {
        Jogo jogoFuturoSemResultado = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(45));
        jogoFuturoSemResultado.setGolsEquipe1(null);
        jogoFuturoSemResultado.setGolsEquipe2(null);

        Jogo jogoPassado = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).minusMinutes(5));
        jogoPassado.setGolsEquipe1(null);
        jogoPassado.setGolsEquipe2(null);

        Jogo jogoComResultado = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(45));
        jogoComResultado.setGolsEquipe1(1);
        jogoComResultado.setGolsEquipe2(0);

        assertThat(jogoFuturoSemResultado.getPodeExcluirAdministrativo()).isTrue();
        assertThat(jogoPassado.getPodeExcluirAdministrativo()).isFalse();
        assertThat(jogoComResultado.getPodeExcluirAdministrativo()).isFalse();
    }

    @Test
    void devePermitirVisualizarPalpitesDoGrupoQuandoJanelaEncerrada() {
        // Janela de palpites encerrada: falta menos de 1h para o jogo (podeDarPalpite = false)
        ZonedDateTime menosDeUmaHora = ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30);
        Jogo jogoComJanelaEncerrada = criarJogo(menosDeUmaHora);

        assertThat(jogoComJanelaEncerrada.getPodeDarPalpite()).isFalse();
        assertThat(jogoComJanelaEncerrada.getPodeVerPalpitesGrupo()).isTrue();
    }

    @Test
    void deveBloquearVisualizacaoDePalpitesDoGrupoQuandoJanelaAberta() {
        // Janela de palpites aberta: falta mais de 1h para o jogo (podeDarPalpite = true)
        ZonedDateTime maisDeUmaHora = ZonedDateTime.now(BolaoTime.getZoneId()).plusHours(5);
        Jogo jogoComJanelaAberta = criarJogo(maisDeUmaHora);

        assertThat(jogoComJanelaAberta.getPodeDarPalpite()).isTrue();
        assertThat(jogoComJanelaAberta.getPodeVerPalpitesGrupo()).isFalse();
    }

    @Test
    void deveRetornarRelacaoInversaEntrePodeDarPalpiteEPodeVerPalpitesGrupo() {
        ZonedDateTime futuro = ZonedDateTime.now(BolaoTime.getZoneId()).plusHours(3);
        Jogo jogoFuturo = criarJogo(futuro);

        ZonedDateTime passado = ZonedDateTime.now(BolaoTime.getZoneId()).minusHours(2);
        Jogo jogoPassado = criarJogo(passado);

        // Relação inversa deve valer para ambos
        assertThat(jogoFuturo.getPodeDarPalpite()).isNotEqualTo(jogoFuturo.getPodeVerPalpitesGrupo());
        assertThat(jogoPassado.getPodeDarPalpite()).isNotEqualTo(jogoPassado.getPodeVerPalpitesGrupo());
    }

    @Test
    void devePermitirVisualizarPalpitesDeJogoJaOcorrido() {
        // Jogo que já ocorreu: janela de palpites está encerrada
        ZonedDateTime passado = ZonedDateTime.now(BolaoTime.getZoneId()).minusHours(2);
        Jogo jogoOcorrido = criarJogo(passado);

        assertThat(jogoOcorrido.getPodeDarPalpite()).isFalse();
        assertThat(jogoOcorrido.getPodeVerPalpitesGrupo()).isTrue();
    }

    @Test
    void deveBloquearVisualizacaoDePalpitesQuandoDataHoraForNula() {
        // Segurança: dataHora nula não deve expor palpites
        Jogo jogoSemData = new Jogo();
        // Não definir data/hora — getDataHora() retornará null

        assertThat(jogoSemData.getDataHora()).isNull();
        assertThat(jogoSemData.getPodeDarPalpite()).isFalse();
        assertThat(jogoSemData.getPodeVerPalpitesGrupo()).isFalse();
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
