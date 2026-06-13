package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.apache.struts2.ActionSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.util.BolaoTime;
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
    void deveAplicarFiltroPadraoAteHoje() {
        Date dataLimiteEsperada = Date.from(LocalDate.now(BolaoTime.getZoneId())
                .atStartOfDay(BolaoTime.getZoneId()).toInstant());
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(java.util.List.of());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(java.util.List.of());

        String result = adminAction.carregarJogos();

        assertThat(result).isEqualTo(ActionSupport.SUCCESS);
        verify(jogoService).buscarUsandoFiltro(argThat(filtro -> filtro != null
                && filtro.getDataInicial() == null
                && filtro.getDataFinal() != null
                && !filtro.isSoJogosQueNaoOcorreram()));
        verify(httpRequest).setAttribute(eq("adminFiltroDataLimite"),
                argThat((Date d) -> d != null && d.equals(dataLimiteEsperada)));
        verify(httpRequest).setAttribute("adminFiltroAteHojeAtivo", Boolean.TRUE);
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
}
