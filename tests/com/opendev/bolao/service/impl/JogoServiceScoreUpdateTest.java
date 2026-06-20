package com.opendev.bolao.service.impl;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.service.JogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JogoServiceScoreUpdateTest {

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private JogoServiceImpl jogoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpdateScoreWhenChanged() {
        Jogo jogo = new Jogo();
        jogo.setId(1L);
        jogo.setExternalId("123");
        jogo.setGolsEquipe1(0);
        jogo.setGolsEquipe2(0);

        when(jogoRepository.findByExternalId("123")).thenReturn(Optional.of(jogo));

        jogoService.processarAtualizacaoScore("123", 1, 0, Instant.now());

        verify(jogoRepository, times(1)).save(jogo);
        assert jogo.getGolsEquipe1() == 1;
        assert jogo.getGolsEquipe2() == 0;
    }

    @Test
    void shouldNotInvalidateCacheIfScoreIsSame() {
        Jogo jogo = new Jogo();
        jogo.setId(1L);
        jogo.setExternalId("123");
        jogo.setGolsEquipe1(1);
        jogo.setGolsEquipe2(1);

        when(jogoRepository.findByExternalId("123")).thenReturn(Optional.of(jogo));

        jogoService.processarAtualizacaoScore("123", 1, 1, Instant.now());

        verify(jogoRepository, times(1)).save(jogo);
    }
}
