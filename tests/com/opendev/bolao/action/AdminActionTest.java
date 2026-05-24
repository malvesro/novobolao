package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AdminActionTest {

    @Mock
    private JogoService jogoService;

    @Mock
    private EquipeService equipeService;

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AdminAction adminAction;

    @BeforeEach
    void setUp() {
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
        adminAction.setData("2026-06-11");
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
}
