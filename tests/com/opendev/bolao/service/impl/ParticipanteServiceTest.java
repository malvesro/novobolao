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

import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.util.DadosClassificacao;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ParticipanteServiceTest {

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private JogoRepository jogoRepository;

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

        participanteService = new ParticipanteServiceImpl() {
            @Override
            protected Email criarEmail(String template, String assunto) {
                templateCapturado = template;
                assuntoCapturado = assunto;
                return emailMock;
            }
        };
        participanteService.setParticipanteRepository(participanteRepository);
        participanteService.setJogoRepository(jogoRepository);
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

        Mockito.verify(participanteRepository).save(participante);
        Mockito.verify(emailMock).adicionarEnderecoDestino("deinf.rochett@bc");
        Mockito.verify(emailMock).adicionarEnderecoDestino("rosner.suporte.deinf@bcb.gov.br");
        Mockito.verify(emailMock).setPropriedade("nome", participante.getNome());
        Mockito.verify(emailMock).enviar();
    }

    @Test
    void devePropagarTotalDeJogosParaDadosClassificacao() {
        DadosClassificacao dados = new DadosClassificacao();
        Participante participanteMock = Mockito.mock(Participante.class);
        when(participanteMock.getPontuacaoTotal()).thenReturn(dados);
        when(participanteRepository.findAll()).thenReturn(List.of(participanteMock));
        when(jogoRepository.countJogosFinalizados()).thenReturn(7L);

        List<Participante> resultado = participanteService.buscarClassificacao();

        assertThat(resultado).containsExactly(participanteMock);
        assertThat(dados.getTotalDeJogos()).isEqualTo(7);
        Mockito.verify(jogoRepository).countJogosFinalizados();
    }
}
