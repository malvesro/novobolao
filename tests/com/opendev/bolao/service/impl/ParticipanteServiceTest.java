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

import java.util.ArrayList;
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
    private List<String> templatesCapturados;
    private List<String> assuntosCapturados;
    private Participante participante;

    @BeforeEach
    void setUp() throws Exception {
        participante = new Participante();
        participante.setLogin("testuser");
        participante.setSenha("password123");
        participante.setNome("Test User");
        participante.setEmail("test@example.com");

        templatesCapturados = new ArrayList<>();
        assuntosCapturados = new ArrayList<>();

        emailMock = Mockito.mock(Email.class);

        participanteService = new ParticipanteServiceImpl() {
            @Override
            protected Email criarEmail(String template, String assunto) {
                templatesCapturados.add(template);
                assuntosCapturados.add(assunto);
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

        // Valida que dois e-mails foram criados
        assertThat(templatesCapturados).containsExactly("novoCadastro.html", "pedidoRecebido.html");
        assertThat(assuntosCapturados).containsExactly("Novo pedido de cadastro pendente", "Pedido de cadastro recebido");

        Mockito.verify(participanteRepository).save(participante);
        
        // Captura todos os endereços de destino chamados
        org.mockito.ArgumentCaptor<String> emailCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailMock, Mockito.atLeastOnce()).adicionarEnderecoDestino(emailCaptor.capture());
        
        List<String> emailsEnviados = emailCaptor.getAllValues();
        assertThat(emailsEnviados).contains("test@example.com");
        
        Mockito.verify(emailMock, Mockito.atLeast(2)).setPropriedade("nome", participante.getNome());
        Mockito.verify(emailMock, Mockito.atLeast(2)).enviar();
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
