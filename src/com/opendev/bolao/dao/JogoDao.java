package com.opendev.bolao.dao;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.util.FiltroBuscaJogos;

public interface JogoDao {

	public Long salvar(Jogo jogo);
	
	public List buscarTodos();
	
	public Jogo buscarPorId(Long id);
	
	public long buscarQuantidadeDeJogosOcorridos();
	
	public List buscarJogosOcorridos();
	
	public List buscarUsandoFiltro(FiltroBuscaJogos filtro);

    public List buscarPorDataEHora(Date data, Time hora);
    
    public List buscarPorData(Date data);
    
    public List buscarJogosDaEquipe(Long id, int[] fases);
	
}
