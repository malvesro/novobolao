package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Field;

import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.DadosClassificacao;
import com.opendev.bolao.util.FiltroBuscaJogos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        when(jogoService.contarJogosUsandoFiltro(null)).thenReturn(104L);
        when(equipeService.buscarApenasPaisesReais()).thenReturn(new ArrayList<>());

        action.prepararInfoPalpites();

        assertThat(action.getFiltro()).isNotNull();
        assertThat(action.getFiltro().getDataInicial()).isEqualTo(amanha);
        assertThat(action.getFiltro().getDataFinal()).isEqualTo(amanha);
        assertThat(action.getProgressoPalpitesTotal()).isEqualTo(104);
        
        // Verifica que busca o total global para o progresso
        verify(jogoService).contarJogosUsandoFiltro(null);
        verify(jogoService).buscarUsandoFiltro(any(FiltroBuscaJogos.class));
    }

    @Test
    void deveUsarInicioDoDiaNoTimezoneDoBrasilComoReferenciaDaCargaMinima() {
        Date proximaData = new Date(System.currentTimeMillis() + 86400000L);
        when(jogoService.buscarPrimeiraDataComJogosApos(any(Date.class))).thenReturn(proximaData);
        when(jogoService.buscarUsandoFiltro(any(FiltroBuscaJogos.class))).thenReturn(new ArrayList<>());
        when(jogoService.contarJogosUsandoFiltro(null)).thenReturn(104L);
        when(equipeService.buscarApenasPaisesReais()).thenReturn(new ArrayList<>());

        action.prepararInfoPalpites();

        ArgumentCaptor<Date> dataCaptor = ArgumentCaptor.forClass(Date.class);
        verify(jogoService).buscarPrimeiraDataComJogosApos(dataCaptor.capture());
        Date dataReferencia = dataCaptor.getValue();

        Date inicioDoDiaBrasil = Date.from(LocalDate.now(BolaoTime.getZoneId())
                .atStartOfDay(BolaoTime.getZoneId())
                .toInstant());

        assertThat(dataReferencia).isEqualTo(inicioDoDiaBrasil);
    }

    @Test
    void deveCarregarResumoDeLideresTop3ComOrdenacaoOficial() throws Exception {
        ArrayList<Participante> classificacao = new ArrayList<>();
        classificacao.add(criarParticipante("Bruno Souza", 15, 2, 1));
        classificacao.add(criarParticipante("Alice Silva", 15, 3, 1));
        classificacao.add(criarParticipante("Carlos Lima", 12, 9, 9));
        classificacao.add(criarParticipante("Daniel Rocha", 11, 9, 9));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacao);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getLideresResumo()).hasSize(3);

        Participante primeiro = (Participante) action.getLideresResumo().get(0);
        Participante segundo = (Participante) action.getLideresResumo().get(1);
        Participante terceiro = (Participante) action.getLideresResumo().get(2);

        assertThat(primeiro.getNomeFormatado()).isEqualTo("Alice Silva");
        assertThat(segundo.getNomeFormatado()).isEqualTo("Bruno Souza");
        assertThat(terceiro.getNomeFormatado()).isEqualTo("Carlos Lima");
    }

    @Test
    void deveManterResumoEstavelQuandoTodosEstaoComZeroPontos() throws Exception {
        ArrayList<Participante> classificacaoInicial = new ArrayList<>();
        classificacaoInicial.add(criarParticipante("Zeca Alves", 0, 0, 0));
        classificacaoInicial.add(criarParticipante("Bruno Souza", 0, 0, 0));
        classificacaoInicial.add(criarParticipante("Alice Silva", 0, 0, 0));
        classificacaoInicial.add(criarParticipante("Carlos Lima", 0, 0, 0));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacaoInicial);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getLideresResumo()).hasSize(3);

        Participante primeiro = (Participante) action.getLideresResumo().get(0);
        Participante segundo = (Participante) action.getLideresResumo().get(1);
        Participante terceiro = (Participante) action.getLideresResumo().get(2);

        System.out.println("DEBUG: 1=" + primeiro.getNomeFormatado() + ", 2=" + segundo.getNomeFormatado() + ", 3=" + terceiro.getNomeFormatado());

        // Com todos os critérios numéricos zerados, o fallback oficial é alfabético.
        assertThat(primeiro.getNomeFormatado()).isEqualTo("Alice Silva");
        assertThat(segundo.getNomeFormatado()).isEqualTo("Bruno Souza");
        assertThat(terceiro.getNomeFormatado()).isEqualTo("Carlos Lima");
    }

    @Test
    void deveSinalizarQuandoDesempateFoiAplicadoNoTopo() throws Exception {
        ArrayList<Participante> classificacao = new ArrayList<>();
        classificacao.add(criarParticipante("Bruno Souza", 12, 2, 1));
        classificacao.add(criarParticipante("Alice Silva", 12, 3, 1));
        classificacao.add(criarParticipante("Carlos Lima", 9, 0, 0));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacao);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.isLiderancaDesempateAplicado()).isTrue();
        assertThat(action.getLiderancaEmpatadosMesmoPontosRestantes()).isZero();
    }

    @Test
    void deveInformarQuantidadeAdicionalQuandoEmpateNoTopoExcedeTop3() throws Exception {
        ArrayList<Participante> classificacao = new ArrayList<>();
        classificacao.add(criarParticipante("Bruno Souza", 0, 0, 0));
        classificacao.add(criarParticipante("Alice Silva", 0, 0, 0));
        classificacao.add(criarParticipante("Carlos Lima", 0, 0, 0));
        classificacao.add(criarParticipante("Daniel Rocha", 0, 0, 0));
        classificacao.add(criarParticipante("Eduardo Costa", 0, 0, 0));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacao);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getLideresResumo()).hasSize(3);
        assertThat(action.isLiderancaDesempateAplicado()).isTrue();
        assertThat(action.getLiderancaEmpatadosMesmoPontosRestantes()).isEqualTo(2);
    }

    @Test
    void deveManterResumoEstavelQuandoTodosEmpatadosDentroDoTop3() throws Exception {
        ArrayList<Participante> classificacao = new ArrayList<>();
        classificacao.add(criarParticipante("Bruno Souza", 7, 1, 0));
        classificacao.add(criarParticipante("Alice Silva", 7, 1, 0));
        classificacao.add(criarParticipante("Carlos Lima", 7, 1, 0));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacao);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getLideresResumo()).hasSize(3);
        assertThat(action.isLiderancaDesempateAplicado()).isTrue();
        assertThat(action.getLiderancaEmpatadosMesmoPontosRestantes()).isZero();

        Participante primeiro = (Participante) action.getLideresResumo().get(0);
        Participante segundo = (Participante) action.getLideresResumo().get(1);
        Participante terceiro = (Participante) action.getLideresResumo().get(2);

        assertThat(primeiro.getNomeFormatado()).isEqualTo("Alice Silva");
        assertThat(segundo.getNomeFormatado()).isEqualTo("Bruno Souza");
        assertThat(terceiro.getNomeFormatado()).isEqualTo("Carlos Lima");
    }

    @Test
    void deveSinalizarEmpateParcialNoPodioSemContagemAdicional() throws Exception {
        ArrayList<Participante> classificacao = new ArrayList<>();
        classificacao.add(criarParticipante("Bruno Souza", 12, 2, 1));
        classificacao.add(criarParticipante("Alice Silva", 12, 3, 1));
        classificacao.add(criarParticipante("Carlos Lima", 10, 1, 0));
        classificacao.add(criarParticipante("Daniel Rocha", 8, 1, 0));

        when(jogoService.buscarJogosDeHoje()).thenReturn(new ArrayList<>());
        
        // Simular o comportamento do serviço que já retorna a lista ordenada
        ArrayList<Participante> classificacaoOrdenada = new ArrayList<>(classificacao);
        Collections.sort(classificacaoOrdenada);
        when(participanteService.buscarClassificacao()).thenReturn(classificacaoOrdenada);

        String resultado = action.obterDadosPaginaPrincipal();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getLideresResumo()).hasSize(3);
        assertThat(action.isLiderancaDesempateAplicado()).isTrue();
        assertThat(action.getLiderancaEmpatadosMesmoPontosRestantes()).isZero();

        Participante primeiro = (Participante) action.getLideresResumo().get(0);
        Participante segundo = (Participante) action.getLideresResumo().get(1);
        Participante terceiro = (Participante) action.getLideresResumo().get(2);

        assertThat(primeiro.getNomeFormatado()).isEqualTo("Alice Silva");
        assertThat(segundo.getNomeFormatado()).isEqualTo("Bruno Souza");
        assertThat(terceiro.getNomeFormatado()).isEqualTo("Carlos Lima");
    }

    private Participante criarParticipante(String nome, int pontos, int acertosTotais, int acertosParciaisBonus)
            throws Exception {
        Participante participante = new Participante();
        participante.setNome(nome);

        DadosClassificacao dados = new DadosClassificacao();
        dados.setPontuacao(pontos);
        dados.setQuantidadeDeAcertosTotais(acertosTotais);
        dados.setQuantidadeDeAcertosParciaisComBonus(acertosParciaisBonus);

        Field campoPontuacaoTotal = Participante.class.getDeclaredField("pontuacaoTotal");
        campoPontuacaoTotal.setAccessible(true);
        campoPontuacaoTotal.set(participante, dados);
        return participante;
    }
}
