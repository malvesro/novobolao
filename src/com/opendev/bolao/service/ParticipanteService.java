package com.opendev.bolao.service;

import java.util.List;

import com.opendev.bolao.exception.ValidacaoException;
/*
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
*/
import com.opendev.bolao.model.Participante;


public interface ParticipanteService {
    
    public Participante criarNovo(Participante participante) throws ValidacaoException;
	
	public List buscarClassificacao();
	
	public Participante buscarPorLogin(String login);

    public List buscarTodos();
    
    public void atualizarAutorizacao(Long id, boolean autorizado);
    
    public void atualizarPapel(Long id, String papel);

    public void apagar(Long id);
    
/*
    public GraficoComparativoDesempenho construirGraficoDesempenho(Participante participante, Long idRivail);
    
    public GraficoBarraLideres construirGraficoDeBarrasDosLideres();
*/

}
