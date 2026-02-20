package com.opendev.bolao.action;

import java.util.List;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.ConversaoUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import jakarta.servlet.http.HttpServletResponse;

public class AdminAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private EquipeService equipeService;
	private JogoService jogoService;
    private ParticipanteService participanteService;
	
	private List equipes;
	private List jogos;
    private List participantes;
    private Long id;
	private String papel;
	private String status;
    private Integer golsEquipe1;
    private Integer golsEquipe2;
    private String data;
    private String hora;
    private Long equipe1Id;
    private Long equipe2Id;
    private String local;
    private Integer fase;
	
	
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

	public String atualizarResultadoDoJogoHtmx() {
		HttpServletResponse response = ServletActionContext.getResponse();
		if (response == null) {
			return NONE;
		}
		int statusCode = HttpServletResponse.SC_NO_CONTENT;
		try {
			atualizarResultadoDoJogo(this.id, this.golsEquipe1, this.golsEquipe2);
		} catch (IllegalArgumentException ex) {
			statusCode = HttpServletResponse.SC_BAD_REQUEST;
		} catch (Exception ex) {
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		response.setStatus(statusCode);
		return NONE;
	}

	public String criarNovoJogoHtmx() {
		HttpServletResponse response = ServletActionContext.getResponse();
		if (response == null) {
			return NONE;
		}
		int statusCode = HttpServletResponse.SC_NO_CONTENT;
		try {
			if (this.data == null || this.hora == null || this.equipe1Id == null || this.equipe2Id == null
					|| this.local == null || this.fase == null) {
				throw new IllegalArgumentException();
			}
			criarNovoJogo(this.data, this.hora, this.equipe1Id, this.equipe2Id, this.local, this.fase.intValue());
		} catch (IllegalArgumentException ex) {
			statusCode = HttpServletResponse.SC_BAD_REQUEST;
		} catch (Exception ex) {
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		response.setStatus(statusCode);
		return NONE;
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
        this.participantes = getParticipanteService().buscarTodos();
        return SUCCESS;
    }
    
    public void autorizarParticipante(Long id, boolean autorizar) {
        getParticipanteService().atualizarAutorizacao(id, autorizar);
    }

    public void atualizarPapelParticipante(Long id, String papel) {
        getParticipanteService().atualizarPapel(id, papel);
    }

	public String atualizarPapelParticipanteHtmx() {
		getParticipanteService().atualizarPapel(id, papel);
		this.participantes = getParticipanteService().buscarTodos();
		return SUCCESS;
	}

	public String atualizarStatusParticipanteHtmx() {
		boolean habilitado = "true".equalsIgnoreCase(status) || "Sim".equalsIgnoreCase(status);
		getParticipanteService().atualizarAutorizacao(id, habilitado);
		this.participantes = getParticipanteService().buscarTodos();
		return SUCCESS;
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

	@StrutsParameter
	public void setId(Long id) {
		this.id = id;
	}

	@StrutsParameter
	public void setPapel(String papel) {
		this.papel = papel;
	}

	@StrutsParameter
	public void setStatus(String status) {
		this.status = status;
	}

	@StrutsParameter
	public void setGolsEquipe1(Integer golsEquipe1) {
		this.golsEquipe1 = golsEquipe1;
	}

	@StrutsParameter
	public void setGolsEquipe2(Integer golsEquipe2) {
		this.golsEquipe2 = golsEquipe2;
	}

	@StrutsParameter
	public void setData(String data) {
		this.data = data;
	}

	@StrutsParameter
	public void setHora(String hora) {
		this.hora = hora;
	}

	@StrutsParameter
	public void setEquipe1Id(Long equipe1Id) {
		this.equipe1Id = equipe1Id;
	}

	@StrutsParameter
	public void setEquipe2Id(Long equipe2Id) {
		this.equipe2Id = equipe2Id;
	}

	@StrutsParameter
	public void setLocal(String local) {
		this.local = local;
	}

	@StrutsParameter
	public void setFase(Integer fase) {
		this.fase = fase;
	}

}
