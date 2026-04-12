package com.opendev.bolao.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.SanitizationUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.action.ServletResponseAware;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	private static final long serialVersionUID = 1L;
	private static final String PARTICIPANTES_FRAGMENT_RESULT = "fragment";
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminAction.class);
	
	private EquipeService equipeService;
	private JogoService jogoService;
	private ParticipanteService participanteService;
	private transient HttpServletRequest httpRequest;
	private transient HttpServletResponse httpResponse;
	
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
		HttpServletResponse response = getHttpResponse();
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
		HttpServletResponse response = getHttpResponse();
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
	
	public String prepararEdicaoEstruturalHtmx() {
		this.jogos = List.of(getJogoService().buscarPorId(this.id).orElseThrow());
		this.equipes = getEquipeService().buscarTodasEquipes();
		markSkipTemplate();
		return SUCCESS;
	}

	public String salvarEdicaoEstruturalHtmx() {
		HttpServletResponse response = getHttpResponse();
		if (response == null) {
			return NONE;
		}
		int statusCode = HttpServletResponse.SC_NO_CONTENT;
		try {
			if (this.id == null || this.data == null || this.hora == null || this.equipe1Id == null 
					|| this.equipe2Id == null || this.local == null || this.fase == null) {
				throw new IllegalArgumentException();
			}
			getJogoService().atualizarDadosEstruturaisJogo(
					this.id, 
					ConversaoUtils.converterParaData(this.data), 
					ConversaoUtils.converterParaTempo(this.hora), 
					this.local, 
					this.fase, 
					this.equipe1Id, 
					this.equipe2Id
			);
		} catch (IllegalArgumentException ex) {
			statusCode = HttpServletResponse.SC_BAD_REQUEST;
		} catch (Exception ex) {
			LOGGER.error("Erro ao salvar edição estrutural do jogo", ex);
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		response.setStatus(statusCode);
		return NONE;
	}

	public String carregarLinhaJogoHtmx() {
		this.jogos = List.of(getJogoService().buscarPorId(this.id).orElseThrow());
		markSkipTemplate();
		return SUCCESS;
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
		boolean htmx = isHtmxRequest();
		logRequestHeaders("apagarParticipante", htmx);
		if (htmx) {
			markSkipTemplate();
			return PARTICIPANTES_FRAGMENT_RESULT;
		}
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
		boolean htmx = isHtmxRequest();
		logRequestHeaders("atualizarPapelParticipante", htmx);
		if (htmx) {
			markSkipTemplate();
			return PARTICIPANTES_FRAGMENT_RESULT;
		}
		return SUCCESS;
	}

	public String atualizarStatusParticipanteHtmx() {
		boolean habilitado = "true".equalsIgnoreCase(status) || "Sim".equalsIgnoreCase(status);
		getParticipanteService().atualizarAutorizacao(id, habilitado);
		this.participantes = getParticipanteService().buscarTodos();
		boolean htmx = isHtmxRequest();
		logRequestHeaders("atualizarStatusParticipante", htmx);
		if (htmx) {
			markSkipTemplate();
			return PARTICIPANTES_FRAGMENT_RESULT;
		}
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
		this.papel = SanitizationUtils.cleanText(papel, 32);
	}

	@StrutsParameter
	public void setStatus(String status) {
		this.status = SanitizationUtils.cleanText(status, 16);
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
		this.data = SanitizationUtils.cleanText(data, 16);
	}

	@StrutsParameter
	public void setHora(String hora) {
		this.hora = SanitizationUtils.cleanText(hora, 16);
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
		this.local = SanitizationUtils.cleanText(local, 120);
	}

	@StrutsParameter
	public void setFase(Integer fase) {
		this.fase = fase;
	}

	private boolean isHtmxRequest() {
		HttpServletRequest request = getHttpRequest();
		if (request == null) {
			return false;
		}
		String hxRequest = request.getHeader("HX-Request");
		if (hxRequest != null && "true".equalsIgnoreCase(hxRequest)) {
			return true;
		}
		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null && "XMLHttpRequest".equalsIgnoreCase(requestedWith);
	}

	private void logRequestHeaders(String context, boolean htmxDetected) {
		HttpServletRequest request = getHttpRequest();
		if (request == null) {
			LOGGER.warn("[HTMX-TRACE][action={}] HttpServletRequest indisponível para instrumentação.", context);
			return;
		}
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("HX-Request", sanitizeHeaderValue(request.getHeader("HX-Request")));
		headers.put("X-Requested-With", sanitizeHeaderValue(request.getHeader("X-Requested-With")));
		headers.put("Sec-Fetch-Site", sanitizeHeaderValue(request.getHeader("Sec-Fetch-Site")));
		headers.put("Sec-Fetch-Mode", sanitizeHeaderValue(request.getHeader("Sec-Fetch-Mode")));
		headers.put("Sec-Fetch-Dest", sanitizeHeaderValue(request.getHeader("Sec-Fetch-Dest")));
		headers.put("Sec-Fetch-User", sanitizeHeaderValue(request.getHeader("Sec-Fetch-User")));
		headers.put("Origin", sanitizeHeaderValue(request.getHeader("Origin")));
		headers.put("Referer", sanitizeHeaderValue(request.getHeader("Referer")));
		boolean hasValue = false;
		for (String value : headers.values()) {
			if (value != null && !value.isEmpty()) {
				hasValue = true;
				break;
			}
		}
		if (!htmxDetected && !hasValue) {
			return;
		}
		LOGGER.info("[HTMX-TRACE][action={}] detected={} headers={}", context, htmxDetected, headers);
	}

	private String sanitizeHeaderValue(String value) {
		if (value == null) {
			return null;
		}
		return value.replaceAll("[\\r\\n]", "");
	}

	private void markSkipTemplate() {
		HttpServletRequest request = getHttpRequest();
		if (request != null) {
			request.setAttribute("skipTemplate", Boolean.TRUE);
		}
	}

	@Override
	public void withServletRequest(HttpServletRequest request) {
		this.httpRequest = request;
	}

	@Override
	public void withServletResponse(HttpServletResponse response) {
		this.httpResponse = response;
	}

	private HttpServletRequest getHttpRequest() {
		return this.httpRequest;
	}

	private HttpServletResponse getHttpResponse() {
		return this.httpResponse;
	}

}
