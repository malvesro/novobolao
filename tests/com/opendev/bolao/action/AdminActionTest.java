package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.struts2.ActionSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AdminActionTest {

    @Mock
    private JogoService jogoService;

    @Mock
    private EquipeService equipeService;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AdminAction adminAction;

    @BeforeEach
    void setUp() {
        adminAction.withServletRequest(httpRequest);
        adminAction.withServletResponse(httpResponse);
    }

    @Test
    void devePrepararEdicaoEstruturalComSucesso() {
        adminAction.setId(1L);
        Jogo jogo = new Jogo();
        jogo.setId(1L);

        when(jogoService.buscarPorId(1L)).thenReturn(Optional.of(jogo));
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.prepararEdicaoEstruturalHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        assertThat(adminAction.getJogos()).containsExactly(jogo);
        verify(jogoService).buscarPorId(1L);
        verify(equipeService).buscarApenasPaisesReais();
    }

    @Test
    void deveRetornarBadRequestAoPrepararSemId() {
        adminAction.setId(null);

        String result = adminAction.prepararEdicaoEstruturalHtmx();

        assertThat(result).isEqualTo(ActionSupport.NONE);
        verify(httpResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void deveSalvarEdicaoEstruturalComSucesso() {
        adminAction.setId(1L);
        // Contrato da UI administrativa envia data no formato brasileiro (dd/MM/yyyy).
        adminAction.setData("11/06/2026");
        adminAction.setHora("15:00");
        adminAction.setEquipe1Id(10L);
        adminAction.setEquipe2Id(20L);
        adminAction.setLocal("Estádio");
        adminAction.setFase(1);

        Jogo jogo = new Jogo();
        jogo.setId(1L);

        when(jogoService.buscarPorId(1L)).thenReturn(Optional.of(jogo));

        String result = adminAction.salvarEdicaoEstruturalHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(jogoService).atualizarDadosEstruturaisJogo(any(), any(), any(), any(), anyInt(), any(), any());
        verify(jogoService).buscarPorId(1L);
    }

    @Test
    void deveRetornarBadRequestAoSalvarEdicaoEstruturalSemData() {
        adminAction.setId(1L);
        adminAction.setData(null);
        adminAction.setHora("15:00");
        adminAction.setEquipe1Id(10L);
        adminAction.setEquipe2Id(20L);
        adminAction.setLocal("Estádio");
        adminAction.setFase(1);

        String result = adminAction.salvarEdicaoEstruturalHtmx();

        assertThat(result).isEqualTo(ActionSupport.NONE);
        verify(httpResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(jogoService, never()).atualizarDadosEstruturaisJogo(any(), any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    void deveRetornarBadRequestAoSalvarEdicaoEstruturalComDataInvalida() {
        adminAction.setId(1L);
        adminAction.setData("99/99/2026");
        adminAction.setHora("15:00");
        adminAction.setEquipe1Id(10L);
        adminAction.setEquipe2Id(20L);
        adminAction.setLocal("Estádio");
        adminAction.setFase(1);

        String result = adminAction.salvarEdicaoEstruturalHtmx();

        assertThat(result).isEqualTo(ActionSupport.NONE);
        verify(httpResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(jogoService, never()).atualizarDadosEstruturaisJogo(any(), any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    void deveMarcarContextoAdminAoCarregarJogos() {
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(java.util.List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(httpRequest).setAttribute("adminResultadoView", Boolean.TRUE);
        verify(httpRequest).setAttribute("adminFiltroAteHojeAtivo", Boolean.TRUE);
        verify(jogoService).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
        verify(equipeService).buscarApenasPaisesReais();
    }

    @Test
    void deveAplicarFiltroPadraoDaDataAtual() {
        Date dataAtualEsperada = Date.from(LocalDate.now(BolaoTime.getZoneId())
                .atStartOfDay(BolaoTime.getZoneId()).toInstant());
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(java.util.List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(jogoService).buscarUsandoFiltro(argThat(filtro -> filtro != null
                && filtro.getDataInicial() != null
                && filtro.getDataFinal() != null
                && filtro.getDataInicial().equals(filtro.getDataFinal())
                && !filtro.isSoJogosQueNaoOcorreram()));
        verify(httpRequest).setAttribute(eq("adminFiltroDataLimite"),
                argThat((Date d) -> d != null && d.equals(dataAtualEsperada)));
        verify(httpRequest).setAttribute("adminFiltroAteHojeAtivo", Boolean.TRUE);
    }

    @Test
    void deveAplicarFiltroAdminAlinhadoComPalpitesQuandoSolicitado() {
        adminAction.setUsarFiltro(true);
        adminAction.setDataInicial("11/06/2026");
        adminAction.setDataFinal("20/06/2026");
        adminAction.setFiltroFase(16);
        adminAction.setFiltroEquipe(1L);
        adminAction.setFiltroGrupo("a");
        adminAction.setFiltroJogosNaoOcorreram(true);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(List.of(criarEquipe(1L)));

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService).buscarUsandoFiltro(filtroCaptor.capture());
        FiltroBuscaJogos filtroAplicado = filtroCaptor.getValue();
        assertThat(filtroAplicado.getDataInicial()).isEqualTo(ConversaoUtils.converterParaData("11/06/2026"));
        assertThat(filtroAplicado.getDataFinal()).isEqualTo(ConversaoUtils.converterParaData("20/06/2026"));
        assertThat(filtroAplicado.getFase()).isEqualTo(16);
        assertThat(filtroAplicado.getIdEquipe()).isEqualTo(1L);
        assertThat(filtroAplicado.getGrupo()).isEqualTo("A");
        assertThat(filtroAplicado.isSoJogosQueNaoOcorreram()).isTrue();
        verify(httpRequest).setAttribute("adminFiltroAteHojeAtivo", Boolean.FALSE);
    }

    @Test
    void deveAceitarGrupoIComoValidoNoFiltroAdmin() {
        adminAction.setUsarFiltro(true);
        adminAction.setFiltroGrupo("i");
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService).buscarUsandoFiltro(filtroCaptor.capture());
        assertThat(filtroCaptor.getValue().getGrupo()).isEqualTo("I");
    }

    @Test
    void deveAceitarGrupoLComoValidoNoFiltroAdmin() {
        adminAction.setUsarFiltro(true);
        adminAction.setFiltroGrupo("L");
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService).buscarUsandoFiltro(filtroCaptor.capture());
        assertThat(filtroCaptor.getValue().getGrupo()).isEqualTo("L");
    }

    @Test
    void deveSanitizarFiltroAdminInvalidoComFallbackSeguro() {
        adminAction.setUsarFiltro(true);
        adminAction.setDataInicial("99/99/2026");
        adminAction.setDataFinal("10/06/2026");
        adminAction.setFiltroFase(999);
        adminAction.setFiltroEquipe(999L);
        adminAction.setFiltroGrupo("grupo-invalido");
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(List.of(criarEquipe(1L), criarEquipe(2L)));

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService).buscarUsandoFiltro(filtroCaptor.capture());
        FiltroBuscaJogos filtroAplicado = filtroCaptor.getValue();
        assertThat(filtroAplicado.getDataInicial()).isNull();
        assertThat(filtroAplicado.getDataFinal()).isEqualTo(ConversaoUtils.converterParaData("10/06/2026"));
        assertThat(filtroAplicado.getFase()).isNull();
        assertThat(filtroAplicado.getIdEquipe()).isNull();
        assertThat(filtroAplicado.getGrupo()).isNull();
        assertThat(adminAction.getFiltroAvisos()).isNotEmpty()
                .anySatisfy(aviso -> assertThat(aviso).contains("data inicial"))
                .anySatisfy(aviso -> assertThat(aviso).contains("fase"))
                .anySatisfy(aviso -> assertThat(aviso).contains("equipe"))
                .anySatisfy(aviso -> assertThat(aviso).contains("grupo"));
    }

    @Test
    void devePermitirListagemCompletaQuandoMostrarTodosAtivo() {
        adminAction.setMostrarTodos(true);
        when(jogoService.buscarTodos()).thenReturn(java.util.List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(jogoService).buscarTodos();
        verify(jogoService, never()).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
        verify(httpRequest).setAttribute("adminMostrandoTodos", Boolean.TRUE);
    }

    @Test
    void deveBuscarMaisJogosComSucesso() {
        adminAction.setData("14/06/2026");
        Date proximaData = new Date(); 
        Jogo jogo = new Jogo();
        
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(proximaData);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(java.util.List.of(jogo));
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.buscarMaisJogosHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        assertThat(adminAction.getJogos()).containsExactly(jogo);
        verify(httpRequest).setAttribute("skipTemplate", Boolean.TRUE);
        verify(httpRequest).setAttribute("adminResultadoView", Boolean.TRUE);
        verify(jogoService).buscarPrimeiraDataComJogosApos(any(Date.class));
        verify(jogoService).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
    }

    @Test
    void deveRespeitarFiltrosAtivosAoBuscarMaisJogosNoAdmin() {
        adminAction.setData("14/06/2026");
        adminAction.setUsarFiltro(true);
        adminAction.setFiltroFase(16);
        adminAction.setFiltroEquipe(1L);
        adminAction.setFiltroGrupo("b");
        adminAction.setFiltroJogosNaoOcorreram(true);
        Date proximaData = ConversaoUtils.converterParaData("15/06/2026");
        Jogo jogo = new Jogo();
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(proximaData);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of(jogo));
        when(equipeService.buscarApenasPaisesReais()).thenReturn(List.of(criarEquipe(1L)));

        String result = adminAction.buscarMaisJogosHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService, atLeastOnce()).buscarUsandoFiltro(filtroCaptor.capture());
        FiltroBuscaJogos filtroFinal = filtroCaptor.getAllValues().get(filtroCaptor.getAllValues().size() - 1);
        assertThat(filtroFinal.getDataInicial()).isEqualTo(proximaData);
        assertThat(filtroFinal.getDataFinal()).isEqualTo(proximaData);
        assertThat(filtroFinal.getFase()).isEqualTo(16);
        assertThat(filtroFinal.getIdEquipe()).isEqualTo(1L);
        assertThat(filtroFinal.getGrupo()).isEqualTo("B");
        assertThat(filtroFinal.isSoJogosQueNaoOcorreram()).isTrue();
    }

    @Test
    void deveCarregarProximaDataNoPrimeiroCliqueQuandoAdminEstiverNaCargaPadrao() {
        adminAction.setData("14/06/2026");
        // Simula o comportamento anterior da view, que propagava o período implícito
        // da carga padrão (somente data atual) no primeiro click do load-more.
        adminAction.setDataInicial("14/06/2026");
        adminAction.setDataFinal("14/06/2026");
        Date diaSeguinte = ConversaoUtils.converterParaData("15/06/2026");
        Jogo jogo = new Jogo();
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(diaSeguinte);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of(jogo));
        when(equipeService.buscarApenasPaisesReais()).thenReturn(List.of());

        String result = adminAction.buscarMaisJogosHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        assertThat(adminAction.getJogos()).containsExactly(jogo);
        verify(jogoService).buscarPrimeiraDataComJogosApos(eq(diaSeguinte));
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService).buscarUsandoFiltro(filtroCaptor.capture());
        FiltroBuscaJogos filtroFinal = filtroCaptor.getValue();
        assertThat(filtroFinal.getDataInicial()).isEqualTo(diaSeguinte);
        assertThat(filtroFinal.getDataFinal()).isEqualTo(diaSeguinte);
        assertThat(filtroFinal.getFase()).isNull();
        assertThat(filtroFinal.getIdEquipe()).isNull();
        assertThat(filtroFinal.getGrupo()).isNull();
        assertThat(filtroFinal.isSoJogosQueNaoOcorreram()).isFalse();
    }

    @Test
    void deveInterromperLoadMoreAdminQuandoProximaDataUltrapassarDataFinalDoFiltro() {
        adminAction.setData("14/06/2026");
        adminAction.setUsarFiltro(true);
        adminAction.setDataFinal("15/06/2026");
        Date proximaDataForaDoPeriodo = ConversaoUtils.converterParaData("16/06/2026");
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(proximaDataForaDoPeriodo);

        String result = adminAction.buscarMaisJogosHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        assertThat(adminAction.getJogos()).isNotNull().isEmpty();
        verify(jogoService).buscarPrimeiraDataComJogosApos(any(Date.class));
        verify(jogoService, never()).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
    }

    @Test
    void devePermitirLoadMoreAdminQuandoProximaDataForIgualADataFinalDoFiltro() {
        adminAction.setData("14/06/2026");
        adminAction.setUsarFiltro(true);
        adminAction.setDataFinal("16/06/2026");
        Date dataNoLimiteFinal = ConversaoUtils.converterParaData("16/06/2026");
        Jogo jogo = new Jogo();
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(dataNoLimiteFinal);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(List.of(jogo));
        when(equipeService.buscarApenasPaisesReais()).thenReturn(List.of());

        String result = adminAction.buscarMaisJogosHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        assertThat(adminAction.getJogos()).containsExactly(jogo);
        ArgumentCaptor<FiltroBuscaJogos> filtroCaptor = ArgumentCaptor.forClass(FiltroBuscaJogos.class);
        verify(jogoService, atLeastOnce()).buscarUsandoFiltro(filtroCaptor.capture());
        FiltroBuscaJogos filtroFinal = filtroCaptor.getAllValues().get(filtroCaptor.getAllValues().size() - 1);
        assertThat(filtroFinal.getDataInicial()).isEqualTo(dataNoLimiteFinal);
        assertThat(filtroFinal.getDataFinal()).isEqualTo(dataNoLimiteFinal);
    }

    @Test
    void deveAtualizarResultadoHtmxQuandoJogoJaIniciado() {
        adminAction.setId(1L);
        adminAction.setGolsEquipe1(3);
        adminAction.setGolsEquipe2(1);
        Jogo jogo = criarJogoComInicio(ZonedDateTime.now(BolaoTime.getZoneId()).minusMinutes(1));

        when(jogoService.buscarPorId(1L)).thenReturn(Optional.of(jogo));

        String result = adminAction.atualizarResultadoDoJogoHtmx();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(jogoService).atualizarResultado(1L, 3, 1);
    }

    @Test
    void deveBloquearAtualizacaoDeResultadoAntesDoInicioDoJogo() {
        adminAction.setId(1L);
        adminAction.setGolsEquipe1(3);
        adminAction.setGolsEquipe2(1);
        Jogo jogo = criarJogoComInicio(ZonedDateTime.now(BolaoTime.getZoneId()).plusMinutes(30));

        when(jogoService.buscarPorId(1L)).thenReturn(Optional.of(jogo));

        String result = adminAction.atualizarResultadoDoJogoHtmx();

        assertThat(result).isEqualTo(ActionSupport.NONE);
        verify(httpResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(jogoService, never()).atualizarResultado(anyLong(), anyInt(), anyInt());
    }

    private Jogo criarJogoComInicio(ZonedDateTime dataHoraInicio) {
        Jogo jogo = new Jogo();
        jogo.setId(1L);
        jogo.setData(java.util.Date.from(dataHoraInicio.toInstant()));
        jogo.setHora(Time.valueOf(dataHoraInicio.toLocalTime()));
        return jogo;
    }

    private Equipe criarEquipe(Long id) {
        Equipe equipe = new Equipe();
        equipe.setId(id);
        return equipe;
    }
}
