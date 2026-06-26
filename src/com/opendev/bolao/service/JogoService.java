package com.opendev.bolao.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;
import java.sql.Time;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.util.FiltroBuscaJogos;

public interface JogoService {
	
	public Long criarNovoJogo(Jogo jogo, Long equipe1Id, Long equipe2Id);
	
	public List buscarTodos();

	public void atualizarResultado(Long idJogo, Integer golsEquipe1, Integer golsEquipe2);

    public Optional<Jogo> buscarPorId(Long id);
    
    public void avisarSobreProximoJogo();
    
    public List buscarUsandoFiltro(FiltroBuscaJogos filtro);

    public long contarJogosUsandoFiltro(FiltroBuscaJogos filtro);
    
    public Date buscarPrimeiraDataComJogosApos(Date data);
    
    public List buscarJogosDeHoje();
    
    public void atualizarDadosEstruturaisJogo(Long idJogo, Date data, Time hora, String local, int fase, Long idEquipe1, Long idEquipe2);

    public boolean podeExcluirJogoAdministrativo(Long idJogo);

    public Map<Long, Boolean> mapearElegibilidadeExclusaoAdministrativa(List<Jogo> jogos);

    public void apagarJogoAdministrativo(Long idJogo, String operador);

}
