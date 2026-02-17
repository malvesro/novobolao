package com.opendev.bolao.service.impl;

import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.exception.ValidacaoException;

@ExtendWith(MockitoExtension.class)
public class ParticipanteServiceTest {

    @Mock
    private ParticipanteDao participanteDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ParticipanteServiceImpl participanteService;

    private Participante participante;

    @BeforeEach
    public void setUp() {
        participante = new Participante();
        participante.setLogin("testuser");
        participante.setSenha("password123");
        participante.setNome("Test User");
        participante.setEmail("test@example.com");
    }

    @Test
    public void deveCriarNovoParticipanteComSucesso() throws ValidacaoException {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // When
        Participante resultado = participanteService.criarNovo(participante);

        // Then
        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getSenha()).isEqualTo("encodedPassword");
        Assertions.assertThat(resultado.getLogin()).isEqualTo("testuser");
        Assertions.assertThat((Object)resultado.getDataHoraCadastro()).isNotNull();
        
        Mockito.verify(participanteDao).salvar(participante);
    }
}
