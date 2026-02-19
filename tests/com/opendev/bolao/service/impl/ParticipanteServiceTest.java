package com.opendev.bolao.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.model.Participante;

@ExtendWith(MockitoExtension.class)
class ParticipanteServiceTest {

    @Mock
    private ParticipanteDao participanteDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ParticipanteServiceImpl participanteService;
    private Email emailMock;
    private String templateCapturado;
    private String assuntoCapturado;
    private Participante participante;

    @BeforeEach
    void setUp() throws Exception {
        participante = new Participante();
        participante.setLogin("testuser");
        participante.setSenha("password123");
        participante.setNome("Test User");
        participante.setEmail("test@example.com");

        templateCapturado = null;
        assuntoCapturado = null;

        emailMock = Mockito.mock(Email.class);
        Mockito.doNothing().when(emailMock).enviar();
        Mockito.doNothing().when(emailMock).adicionarEnderecoDestino(Mockito.anyString());
        Mockito.doNothing().when(emailMock).setPropriedade(Mockito.anyString(), Mockito.any());

        participanteService = new ParticipanteServiceImpl() {
            @Override
            protected Email criarEmail(String template, String assunto) {
                templateCapturado = template;
                assuntoCapturado = assunto;
                return emailMock;
            }
        };
        participanteService.setParticipanteDao(participanteDao);
        participanteService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void deveCriarNovoParticipanteComSucesso() throws Exception {
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        Participante resultado = participanteService.criarNovo(participante);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSenha()).isEqualTo("encodedPassword");
        assertThat(resultado.getLogin()).isEqualTo("testuser");
        assertThat((Object) resultado.getDataHoraCadastro()).isNotNull();

        assertThat(templateCapturado).isEqualTo("novoCadastro.html");
        assertThat(assuntoCapturado).isEqualTo("Novo pedido de cadastro pendente");

        Mockito.verify(participanteDao).salvar(participante);
        Mockito.verify(emailMock).adicionarEnderecoDestino("deinf.rochett@bc");
        Mockito.verify(emailMock).adicionarEnderecoDestino("rosner.suporte.deinf@bcb.gov.br");
        Mockito.verify(emailMock).setPropriedade("nome", participante.getNome());
        Mockito.verify(emailMock).enviar();
    }
}
