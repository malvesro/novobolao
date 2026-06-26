package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.GraficoDesempenhoCacheControl;
import com.opendev.bolao.util.MensagemErro;

class ParticipanteActionTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void configurarRequest() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
    }

    @AfterEach
    void limparRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("gerarGraficoLiderancaImagem deve produzir stream PNG")
    void deveGerarGraficoLiderancaImagem() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(15, "Participante A", "Participante A");

        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.construirGraficoDeBarrasDosLideres())
                .thenReturn(new GraficoBarraLideres(dataset));

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);

        String resultado = action.gerarGraficoLiderancaImagem();
        assertThat(resultado).isEqualTo("success");

        InputStream stream = action.getGraficoStream();
        assertThat(stream).isNotNull();
        assertThat(stream.readAllBytes()).isNotEmpty();
    }

    @Test
    @DisplayName("cadastrar deve retornar input quando login ja existir")
    void deveBloquearCadastroComLoginDuplicado() throws Exception {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.buscarPorLogin("usuario"))
                .thenReturn(Optional.of(new Participante()));

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setLogin("usuario");
        action.setNome("Usuario Teste");
        action.setEmail("teste@example.com");
        action.setSenha("Password@123");

        String resultado = action.cadastrar();

        assertThat(resultado).isEqualTo("input");
        List erros = action.getErrosInclusao();
        assertThat(erros).isNotNull();
        assertThat(erros).filteredOn(MensagemErro.class::isInstance)
                .extracting(obj -> ((MensagemErro) obj).getMensagem())
                .contains("Ja existe um cadastro ativo com este login.");
    }

    @Test
    @DisplayName("cadastrar deve retornar input quando e-mail ja existir")
    void deveBloquearCadastroComEmailDuplicado() throws Exception {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.buscarPorLogin("usuario"))
                .thenReturn(Optional.empty());
        when(participanteService.buscarPorEmail("mail@example.com"))
                .thenReturn(Optional.of(new Participante()));

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setLogin("usuario");
        action.setNome("Usuario Teste");
        action.setEmail("mail@example.com");
        action.setSenha("Password@123");

        String resultado = action.cadastrar();

        assertThat(resultado).isEqualTo("input");
        List erros = action.getErrosInclusao();
        assertThat(erros).isNotNull();
        assertThat(erros).filteredOn(MensagemErro.class::isInstance)
                .extracting(obj -> ((MensagemErro) obj).getMensagem())
                .contains("Este e-mail ja esta associado a outro cadastro.");
    }

    @Test
    @DisplayName("obterDadosGraficoJson deve retornar payload e headers de cache privado")
    void deveRetornarPayloadGraficoComCachePrivado() {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);

        Participante participante = new Participante();
        participante.setId(10L);
        participante.setLogin("usuario");
        participante.setNome("Usuario Teste");

        GraficoComparativoDesempenho grafico = Mockito.mock(GraficoComparativoDesempenho.class);
        when(grafico.getSeriesData()).thenReturn(List.of(Map.of("name", "Usuario Teste", "data", List.of())));
        when(grafico.getCategories()).thenReturn(List.of());

        when(participanteService.buscarPorLogin("usuario")).thenReturn(Optional.of(participante));
        when(participanteService.construirGraficoDesempenho(participante, null)).thenReturn(grafico);

        request.setUserPrincipal(() -> "usuario");

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setRival("");

        String resultado = action.obterDadosGraficoJson();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getGraficoData()).containsKeys("series", "categories");
        assertThat(action.getGraficoData()).containsKey("cacheVersion");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("private, max-age=30, must-revalidate");
        assertThat(response.getHeader("Vary")).isEqualTo("Cookie, Accept-Encoding");
        assertThat(response.getHeader("X-Grafico-Cache-Version"))
                .isEqualTo(String.valueOf(GraficoDesempenhoCacheControl.obterVersaoAtual()));
    }

    @Test
    @DisplayName("obterDadosGraficoJson deve retornar listas vazias quando grafico for nulo")
    void deveRetornarPayloadVazioQuandoGraficoForNulo() {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);

        Participante participante = new Participante();
        participante.setId(20L);
        participante.setLogin("usuario");

        when(participanteService.buscarPorLogin("usuario")).thenReturn(Optional.of(participante));
        when(participanteService.construirGraficoDesempenho(participante, null)).thenReturn(null);
        request.setUserPrincipal(() -> "usuario");

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setRival("");

        String resultado = action.obterDadosGraficoJson();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getGraficoData()).containsEntry("series", List.of());
        assertThat(action.getGraficoData()).containsEntry("categories", List.of());
        assertThat(action.getGraficoData()).containsKey("cacheVersion");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("private, max-age=30, must-revalidate");
    }

    @Test
    @DisplayName("obterDadosGraficoJson deve funcionar sem contexto de response")
    void deveRetornarSucessoSemContextoDeResponse() {
        RequestContextHolder.resetRequestAttributes();

        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.buscarPorLogin(null)).thenReturn(Optional.empty());
        when(participanteService.construirGraficoDesempenho(null, null)).thenReturn(null);

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setRival(null);

        String resultado = action.obterDadosGraficoJson();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getGraficoData()).containsEntry("series", List.of());
        assertThat(action.getGraficoData()).containsEntry("categories", List.of());
        assertThat(action.getGraficoData()).containsKey("cacheVersion");
    }

    @Test
    @DisplayName("obterDadosGraficoJson deve retornar apenas versao quando cacheVersionOnly=true")
    void deveRetornarApenasVersaoQuandoSolicitado() {
        request.setParameter("cacheVersionOnly", "true");

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(Mockito.mock(ParticipanteService.class));

        String resultado = action.obterDadosGraficoJson();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getGraficoData()).containsEntry("series", List.of());
        assertThat(action.getGraficoData()).containsKey("cacheVersion");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("private, no-store");
        assertThat(response.getHeader("X-Grafico-Cache-Version"))
                .isEqualTo(String.valueOf(GraficoDesempenhoCacheControl.obterVersaoAtual()));
    }

    @Test
    @DisplayName("listarPalpitesDoJogoHtmx deve retornar lista vazia quando jogoId for nulo")
    void deveRetornarListaVaziaQuandoJogoIdForNulo() {
        PalpiteService palpiteService = Mockito.mock(PalpiteService.class);
        ParticipanteAction action = new ParticipanteAction();
        action.setPalpiteService(palpiteService);

        String resultado = action.listarPalpitesDoJogoHtmx();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getPalpites()).isEmpty();
        verify(palpiteService, never()).buscarPalpitesDoJogo(Mockito.any());
    }

    @Test
    @DisplayName("listarPalpitesDoJogoHtmx deve retornar lista vazia quando jogo nao existir")
    void deveRetornarListaVaziaQuandoJogoNaoExistir() {
        JogoService jogoService = Mockito.mock(JogoService.class);
        PalpiteService palpiteService = Mockito.mock(PalpiteService.class);
        when(jogoService.buscarPorId(99L)).thenReturn(Optional.empty());

        ParticipanteAction action = new ParticipanteAction();
        action.setJogoService(jogoService);
        action.setPalpiteService(palpiteService);
        action.setJogoId(99L);

        String resultado = action.listarPalpitesDoJogoHtmx();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getPalpites()).isEmpty();
        verify(palpiteService, never()).buscarPalpitesDoJogo(Mockito.any());
    }

    @Test
    @DisplayName("listarPalpitesDoJogoHtmx deve bloquear palpites quando janela ainda estiver aberta")
    void deveBloquearPalpitesDoGrupoQuandoJanelaAberta() {
        JogoService jogoService = Mockito.mock(JogoService.class);
        PalpiteService palpiteService = Mockito.mock(PalpiteService.class);
        Jogo jogo = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).plusHours(5));
        when(jogoService.buscarPorId(10L)).thenReturn(Optional.of(jogo));

        ParticipanteAction action = new ParticipanteAction();
        action.setJogoService(jogoService);
        action.setPalpiteService(palpiteService);
        action.setJogoId(10L);

        String resultado = action.listarPalpitesDoJogoHtmx();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getPalpites()).isEmpty();
        verify(palpiteService, never()).buscarPalpitesDoJogo(Mockito.any());
    }

    @Test
    @DisplayName("listarPalpitesDoJogoHtmx deve retornar palpites quando janela estiver encerrada")
    void deveRetornarPalpitesQuandoJanelaEncerrada() {
        JogoService jogoService = Mockito.mock(JogoService.class);
        PalpiteService palpiteService = Mockito.mock(PalpiteService.class);
        Jogo jogo = criarJogo(ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30));
        List<Palpite> palpitesEsperados = List.of(new Palpite());
        when(jogoService.buscarPorId(10L)).thenReturn(Optional.of(jogo));
        when(palpiteService.buscarPalpitesDoJogo(10L)).thenReturn(palpitesEsperados);

        ParticipanteAction action = new ParticipanteAction();
        action.setJogoService(jogoService);
        action.setPalpiteService(palpiteService);
        action.setJogoId(10L);

        String resultado = action.listarPalpitesDoJogoHtmx();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getPalpites()).isEqualTo(palpitesEsperados);
        verify(palpiteService).buscarPalpitesDoJogo(10L);
    }

    private Jogo criarJogo(ZonedDateTime dataHora) {
        Jogo jogo = new Jogo();
        jogo.setData(java.util.Date.from(dataHora.toInstant()));
        jogo.setHora(Time.valueOf(dataHora.toLocalTime()));
        return jogo;
    }
}
