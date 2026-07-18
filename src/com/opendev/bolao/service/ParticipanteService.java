package com.opendev.bolao.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Participante;


public interface ParticipanteService {

    public Participante criarNovo(Participante participante) throws ValidacaoException;

	public List buscarClassificacao();

	public Optional<Participante> buscarPorLogin(String login);

	public Optional<Participante> buscarPorEmail(String email);

    public List buscarTodos();

    public void atualizarAutorizacao(Long id, boolean autorizado);

    public void atualizarPapel(Long id, String papel);

    public void apagar(Long id);

    public void alterarSenha(String login, String senhaAtual, String novaSenha) throws ValidacaoException;

    public GraficoComparativoDesempenho construirGraficoDesempenho(Participante participante, Long idRivail);

    public GraficoBarraLideres construirGraficoDeBarrasDosLideres();

    /**
     * Constrói os dados do dashboard de corrida de pontuação.
     * Retorna um mapa com a lista de frames (jogo a jogo) e o pódio final (top 3).
     */
    public Map<String, Object> construirDadosDashboardCorrida();

}
