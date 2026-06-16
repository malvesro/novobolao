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

import java.lang.reflect.Field;
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

    @Test
    void deveCalcularVariacaoDePosicaoComparandoComSnapshotAnterior() throws Exception {
        Participante p1PrimeiroSnapshot = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 12);
        Participante p2PrimeiroSnapshot = criarParticipanteComPontuacao(2L, "bruno", "Bruno Souza", 9);
        List<Participante> snapshotInicial = List.of(p1PrimeiroSnapshot, p2PrimeiroSnapshot);

        Participante p1SegundoSnapshot = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 8);
        Participante p2SegundoSnapshot = criarParticipanteComPontuacao(2L, "bruno", "Bruno Souza", 14);
        List<Participante> snapshotAtualizado = List.of(p1SegundoSnapshot, p2SegundoSnapshot);

        when(participanteRepository.findAll()).thenReturn(snapshotInicial, snapshotAtualizado);
        when(jogoRepository.countJogosFinalizados()).thenReturn(10L);

        List<Participante> primeiroRanking = participanteService.buscarClassificacao();
        assertThat(primeiroRanking).hasSize(2);
        assertThat(primeiroRanking.get(0).getLogin()).isEqualTo("alice");
        assertThat(primeiroRanking.get(0).getPontuacaoTotal().getVariacaoPosicao()).isNull();
        assertThat(primeiroRanking.get(1).getPontuacaoTotal().getVariacaoPosicao()).isNull();

        Participante.expirarCacheDeClassificacao();
        List<Participante> segundoRanking = participanteService.buscarClassificacao();

        assertThat(segundoRanking).hasSize(2);
        assertThat(segundoRanking.get(0).getLogin()).isEqualTo("bruno");
        assertThat(segundoRanking.get(1).getLogin()).isEqualTo("alice");
        assertThat(segundoRanking.get(0).getPontuacaoTotal().getVariacaoPosicao()).isEqualTo(1);
        assertThat(segundoRanking.get(1).getPontuacaoTotal().getVariacaoPosicao()).isEqualTo(-1);
    }

    @Test
    void deveManterVariacaoZeroQuandoParticipantePermaneceNaMesmaPosicao() throws Exception {
        Participante aliceInicial = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 10);
        Participante brunoInicial = criarParticipanteComPontuacao(2L, "bruno", "Bruno Souza", 7);
        Participante aliceAtual = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 12);
        Participante brunoAtual = criarParticipanteComPontuacao(2L, "bruno", "Bruno Souza", 9);

        when(participanteRepository.findAll()).thenReturn(
                List.of(aliceInicial, brunoInicial),
                List.of(aliceAtual, brunoAtual));
        when(jogoRepository.countJogosFinalizados()).thenReturn(12L);

        participanteService.buscarClassificacao();
        Participante.expirarCacheDeClassificacao();
        List<Participante> rankingAtual = participanteService.buscarClassificacao();

        assertThat(rankingAtual).hasSize(2);
        assertThat(rankingAtual.get(0).getLogin()).isEqualTo("alice");
        assertThat(rankingAtual.get(1).getLogin()).isEqualTo("bruno");
        assertThat(rankingAtual.get(0).getPontuacaoTotal().getVariacaoPosicao()).isEqualTo(0);
        assertThat(rankingAtual.get(1).getPontuacaoTotal().getVariacaoPosicao()).isEqualTo(0);
    }

    @Test
    void deveManterVariacaoNulaQuandoParticipanteNaoPossuiHistoricoNoSnapshotAnterior() throws Exception {
        Participante aliceInicial = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 10);
        Participante aliceAtual = criarParticipanteComPontuacao(1L, "alice", "Alice Silva", 11);
        Participante carlaNova = criarParticipanteComPontuacao(3L, "carla", "Carla Lima", 13);

        when(participanteRepository.findAll()).thenReturn(
                List.of(aliceInicial),
                List.of(aliceAtual, carlaNova));
        when(jogoRepository.countJogosFinalizados()).thenReturn(8L);

        participanteService.buscarClassificacao();
        Participante.expirarCacheDeClassificacao();
        List<Participante> rankingAtual = participanteService.buscarClassificacao();

        assertThat(rankingAtual).hasSize(2);
        Participante participanteNovo = rankingAtual.stream()
                .filter(p -> "carla".equals(p.getLogin()))
                .findFirst()
                .orElseThrow();
        assertThat(participanteNovo.getPontuacaoTotal().getVariacaoPosicao()).isNull();
    }

    @Test
    void deveIgnorarCalculoDeVariacaoQuandoParticipanteNaoPossuiId() throws Exception {
        Participante semIdInicial = criarParticipanteComPontuacao(null, "anonimo", "Sem Id", 15);
        Participante semIdAtual = criarParticipanteComPontuacao(null, "anonimo", "Sem Id", 15);

        when(participanteRepository.findAll()).thenReturn(
                List.of(semIdInicial),
                List.of(semIdAtual));
        when(jogoRepository.countJogosFinalizados()).thenReturn(5L);

        participanteService.buscarClassificacao();
        Participante.expirarCacheDeClassificacao();
        List<Participante> rankingAtual = participanteService.buscarClassificacao();

        assertThat(rankingAtual).hasSize(1);
        assertThat(rankingAtual.get(0).getPontuacaoTotal().getVariacaoPosicao()).isNull();
    }

    private Participante criarParticipanteComPontuacao(Long id, String login, String nome, int pontuacao) throws Exception {
        Participante participante = new Participante();
        participante.setId(id);
        participante.setLogin(login);
        participante.setNome(nome);

        DadosClassificacao dados = new DadosClassificacao();
        dados.setPontuacao(pontuacao);
        dados.setQuantidadeDeAcertosTotais(0);
        dados.setQuantidadeDeAcertosParciaisComBonus(0);

        Field campoPontuacaoTotal = Participante.class.getDeclaredField("pontuacaoTotal");
        campoPontuacaoTotal.setAccessible(true);
        campoPontuacaoTotal.set(participante, dados);

        return participante;
    }
}
