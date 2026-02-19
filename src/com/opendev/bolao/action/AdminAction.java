package com.opendev.bolao.action;

import java.util.List;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.ConversaoUtils;
import org.apache.struts2.ActionSupport;

public class AdminAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private EquipeService equipeService;
	private JogoService jogoService;
    private ParticipanteService participanteService;
	
	private List equipes;
	private List jogos;
    private List participantes;
    private Long id;
	
	
	public String carregarInfoEquipes() {
		this.equipes = getEquipeService().buscarTodasEquipes();		
		return SUCCESS;
	}
	
	public void criarNovoJogo(String data, String hora, Long equipe1Id, Long equipe2Id, String local, int fase) throws Exception {
		Jogo jogo = new Jogo();
		jogo.setData(ConversaoUtils.converterParaData(data));
		jogo.setHora(ConversaoUtils.converterParaTempo(hora));
		jogo.setLocal(local);
		jogo.setFase(fase);
		getJogoService().criarNovoJogo(jogo, equipe1Id, equipe2Id);
	}
	
	public void atualizarResultadoDoJogo(Long idJogo, Integer golsEquipe1, Integer golsEquipe2) {
		if (idJogo == null || golsEquipe1 == null
				|| golsEquipe1.longValue() < 0
				|| golsEquipe2 == null
				|| golsEquipe2.longValue() < 0) {
			throw new IllegalArgumentException();
		}
		getJogoService().atualizarResultado(idJogo, golsEquipe1, golsEquipe2);
	}
	
	public String carregarJogos() {
		this.jogos = getJogoService().buscarTodos();
		return SUCCESS;
	}
    
    public String carregarParticipantes() {
        this.participantes = getParticipanteService().buscarTodos();
        return SUCCESS;
    }
    
    public String apagarParticipante() {
        getParticipanteService().apagar(id);
        return SUCCESS;
    }
    
    public void autorizarParticipante(Long id, boolean autorizar) {
        getParticipanteService().atualizarAutorizacao(id, autorizar);
    }

    public void atualizarPapelParticipante(Long id, String papel) {
        getParticipanteService().atualizarPapel(id, papel);
    }

	public EquipeService getEquipeService() {
		return equipeService;
	}

	public void setEquipeService(EquipeService equipeService) {
		this.equipeService = equipeService;
	}

	public List getEquipes() {
		return equipes;
	}

	public List getJogos() {
		return jogos;
	}

	public JogoService getJogoService() {
		return jogoService;
	}

	public void setJogoService(JogoService jogoService) {
		this.jogoService = jogoService;
	}

    
    public ParticipanteService getParticipanteService() {
        return this.participanteService;
    }

    
    public void setParticipanteService(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }
    
    public List getParticipantes() {
        return this.participantes;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
