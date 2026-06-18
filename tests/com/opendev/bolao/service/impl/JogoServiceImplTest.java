package com.opendev.bolao.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.repository.EquipeRepository;
import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.util.GraficoDesempenhoCacheControl;

@ExtendWith(MockitoExtension.class)
class JogoServiceImplTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @InjectMocks
    private JogoServiceImpl jogoService;

    private Jogo jogo;
    private Equipe equipe1;
    private Equipe equipe2;

    @BeforeEach
    void setUp() {
        jogo = new Jogo();
        jogo.setId(1L);

        equipe1 = new Equipe();
        equipe1.setId(10L);
        equipe1.setNomePais("Brasil");

        equipe2 = new Equipe();
        equipe2.setId(20L);
        equipe2.setNomePais("Argentina");
    }

    @Test
    void deveAtualizarDadosEstruturaisComSucesso() {
        Date novaData = new Date();
        Time novaHora = Time.valueOf("15:00:00");
        String novoLocal = "Estádio Maracanã";
        int novaFase = 1;

        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo));
        when(equipeRepository.findById(10L)).thenReturn(Optional.of(equipe1));
        when(equipeRepository.findById(20L)).thenReturn(Optional.of(equipe2));

        jogoService.atualizarDadosEstruturaisJogo(1L, novaData, novaHora, novoLocal, novaFase, 10L, 20L);

        assertThat(jogo.getData()).isEqualTo(novaData);
        assertThat(jogo.getHora()).isEqualTo(novaHora);
        assertThat(jogo.getLocal()).isEqualTo(novoLocal);
        assertThat(jogo.getFase()).isEqualTo(novaFase);
        assertThat(jogo.getEquipe1()).isEqualTo(equipe1);
        assertThat(jogo.getEquipe2()).isEqualTo(equipe2);

        verify(jogoRepository).save(jogo);
    }

    @Test
    void deveRetornarJogosDeHojeSemChamadaRepetidaAoBanco() {
        // Primeira chamada: miss de cache → deve ir ao banco
        when(jogoRepository.findByData(any(Date.class))).thenReturn(List.of(jogo));

        @SuppressWarnings("unchecked")
        List<Jogo> resultado1 = (List<Jogo>) jogoService.buscarJogosDeHoje();
        @SuppressWarnings("unchecked")
        List<Jogo> resultado2 = (List<Jogo>) jogoService.buscarJogosDeHoje();

        assertThat(resultado1).containsExactly(jogo);
        assertThat(resultado2).isSameAs(resultado1); // Deve ser a MESMA referência de cache
        // Banco consultado apenas uma vez graças ao cache
        verify(jogoRepository, times(1)).findByData(any(Date.class));
    }

    @Test
    void deveInvalidarVersaoGlobalDoCacheGraficoAoAtualizarResultado() {
        long versaoAntes = GraficoDesempenhoCacheControl.obterVersaoAtual();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo));

        jogoService.atualizarResultado(1L, 2, 1);

        assertThat(GraficoDesempenhoCacheControl.obterVersaoAtual()).isGreaterThan(versaoAntes);
        verify(jogoRepository).save(jogo);
    }
}
