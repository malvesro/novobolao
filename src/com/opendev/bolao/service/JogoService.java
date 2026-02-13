package com.opendev.bolao.service;

import java.util.List;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.util.FiltroBuscaJogos;

public interface JogoService {
	
	public Long criarNovoJogo(Jogo jogo, Long equipe1Id, Long equipe2Id);
	
	public List buscarTodos();

	public void atualizarResultado(Long idJogo, Integer golsEquipe1, Integer golsEquipe2);
    
    public void avisarSobreProximoJogo();
    
    public List buscarUsandoFiltro(FiltroBuscaJogos filtro);
    
    public List buscarJogosDeHoje();

}
