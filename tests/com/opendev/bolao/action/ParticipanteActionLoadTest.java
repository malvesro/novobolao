package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;
import java.util.ArrayList;

import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.FiltroBuscaJogos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParticipanteActionLoadTest {

    @Mock
    private JogoService jogoService;
    @Mock
    private EquipeService equipeService;
    @Mock
    private PalpiteService palpiteService;
    @Mock
    private ParticipanteService participanteService;

    @InjectMocks
    private ParticipanteAction action;

    @BeforeEach
    void setUp() {
        action.setUsarFiltro(false);
    }

    @Test
    void deveAplicarCargaMinimaQuandoSemFiltro() {
        Date hoje = new Date();
        Date amanha = new Date(hoje.getTime() + 86400000);
        
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(amanha);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(new ArrayList<>());
        when(equipeService.buscarApenasPaisesReais()).thenReturn(new ArrayList<>());

        action.prepararInfoPalpites();

        assertThat(action.getFiltro()).isNotNull();
        assertThat(action.getFiltro().getDataInicial()).isEqualTo(amanha);
        assertThat(action.getFiltro().getDataFinal()).isEqualTo(amanha);
        verify(jogoService).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
    }
}
