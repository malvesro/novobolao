package com.opendev.bolao.action;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Time;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;
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
	/**
	 * Flag de request para forçar a renderização da linha administrativa na tela
	 * compartilhada de jogos. Evita que a view de admin reutilize, por engano, o
	 * fragmento de palpites do participante.
	 */
	private static final String ADMIN_RESULTADO_VIEW_ATTRIBUTE = "adminResultadoView";
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminAction.class);
	
	private EquipeService equipeService;
	private JogoService jogoService;
	private ParticipanteService participanteService;
	private transient HttpServletRequest httpRequest;
	private transient HttpServletResponse httpResponse;
	
	private List equipes;
	private List jogos;
	private List participantes;
	private Jogo jogo;
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
	private boolean mostrarTodos;

	
	public String carregarInfoEquipes() {
		this.equipes = getEquipeService().buscarApenasPaisesReais();
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
		LOGGER.info("[HTMX-ADMIN][RESULTADO] Atualizando resultado para jogo ID={}", this.id);
		HttpServletResponse response = getHttpResponse();
		if (response == null) {
			return NONE;
		}
		try {
			atualizarResultadoDoJogo(this.id, this.golsEquipe1, this.golsEquipe2);

			// Recarregar dados para retorno do fragmento
			this.jogo = getJogoService().buscarPorId(this.id).orElseThrow(() -> new com.opendev.bolao.exception.BusinessException("Jogo não encontrado"));
			this.jogos = List.of(this.jogo);
			this.equipes = getEquipeService().buscarApenasPaisesReais();
			markSkipTemplate();
			return SUCCESS;
		} catch (IllegalArgumentException ex) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return NONE;
		} catch (com.opendev.bolao.exception.BusinessException ex) {
			LOGGER.warn("[HTMX-ADMIN][RESULTADO] Erro de negócio para ID={}: {}", this.id, ex.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return NONE;
		} catch (Exception ex) {
			LOGGER.error("[HTMX-ADMIN][RESULTADO] Erro ao atualizar ID={}", this.id, ex);
			throw new com.opendev.bolao.exception.SystemException("Erro interno ao atualizar resultado", ex);
		}
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
		LOGGER.info("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Preparando edição para jogo ID={}", this.id);
		try {
			if (this.id == null) {
				LOGGER.warn("[HTMX-ADMIN][EDICAO-ESTRUTURAL] ID do jogo é nulo.");
				if (httpResponse != null) { httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
				return NONE;
			}
			this.jogo = getJogoService().buscarPorId(this.id).orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado"));
			this.jogos = List.of(this.jogo);
			this.equipes = getEquipeService().buscarApenasPaisesReais();
			markSkipTemplate();
			return SUCCESS;
		} catch (Exception ex) {
			LOGGER.error("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Erro ao preparar edição para ID={}", this.id, ex);
			if (httpResponse != null) { httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); }
			return NONE;
		}
	}

	public String salvarEdicaoEstruturalHtmx() {
		LOGGER.info("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Salvando alterações estruturais para jogo ID={}", this.id);
		HttpServletResponse response = getHttpResponse();
		if (response == null) {
			return NONE;
		}
		try {
			if (this.id == null || this.data == null || this.hora == null || this.equipe1Id == null 
					|| this.equipe2Id == null || this.local == null || this.fase == null) {
				LOGGER.warn("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Parâmetros obrigatórios ausentes para ID={}", this.id);
				throw new IllegalArgumentException("Parâmetros obrigatórios ausentes");
			}

			// Validação explícita do contrato de entrada (dd/MM/yyyy e HH:mm).
			// Evita persistir valores nulos quando o parse falha e retorna erro 400 amigável.
			Date dataConvertida = ConversaoUtils.converterParaData(this.data);
			Time horaConvertida = ConversaoUtils.converterParaTempo(this.hora);
			boolean dataInvalida = dataConvertida == null
					|| !this.data.equals(ConversaoUtils.converterParaString(dataConvertida));
			boolean horaInvalida = horaConvertida == null
					|| !this.hora.equals(ConversaoUtils.converterParaString(horaConvertida));
			if (dataInvalida || horaInvalida) {
				LOGGER.warn("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Formato inválido para data/hora (ID={}): data='{}', hora='{}'",
						this.id, this.data, this.hora);
				throw new IllegalArgumentException("Formato inválido para data/hora");
			}

			getJogoService().atualizarDadosEstruturaisJogo(
					this.id, 
					dataConvertida, 
					horaConvertida, 
					this.local, 
					this.fase, 
					this.equipe1Id, 
					this.equipe2Id
			);
			
			// Após salvar, recarregamos o jogo para devolver a linha da tabela atualizada
			this.jogo = getJogoService().buscarPorId(this.id).orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado"));
			this.jogos = List.of(this.jogo);
			this.equipes = getEquipeService().buscarApenasPaisesReais(); // Necessário para os combos na linha
			markSkipTemplate();
			LOGGER.info("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Sucesso ao salvar ID={}, retornando fragmento de linha.", this.id);
			return SUCCESS; 
		} catch (IllegalArgumentException ex) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			LOGGER.warn("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Erro de validação ao salvar ID={}: {}", this.id, ex.getMessage());
			return NONE;
		} catch (Exception ex) {
			LOGGER.error("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Erro interno ao salvar ID={}", this.id, ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return NONE;
		}
	}

	public String carregarLinhaJogoHtmx() {
		LOGGER.info("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Recarregando linha do jogo ID={}", this.id);
		try {
			this.jogo = getJogoService().buscarPorId(this.id).orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado"));
			this.jogos = List.of(this.jogo);
			this.equipes = getEquipeService().buscarApenasPaisesReais(); // Necessário para os combos na linha
			markSkipTemplate();
			return SUCCESS;
		} catch (Exception ex) {
			LOGGER.error("[HTMX-ADMIN][EDICAO-ESTRUTURAL] Erro ao carregar linha para ID={}", this.id, ex);
			if (httpResponse != null) { httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); }
			return NONE;
		}
	}
	
	public String carregarJogos() {
		this.equipes = getEquipeService().buscarApenasPaisesReais(); // Necessário para os combos de edição direta
		// A JSP de jogos é compartilhada entre /seguro e /admin.
		// Marcamos explicitamente o contexto administrativo para o include correto
		// (`admin-match-row.jsp`) e para manter os inputs de resultado ativos.
		markAdminResultadoView();
		carregarJogosComFiltroPadraoAteHoje();
		return SUCCESS;
	}

	public String buscarMaisJogosHtmx() {
		LOGGER.info("[HTMX-ADMIN][LOAD-MORE] Buscando mais jogos a partir da data: {}", this.data);
		markSkipTemplate();
		markAdminResultadoView();
		if (this.data == null) {
			return SUCCESS;
		}

		try {
			Date dataReferencia = ConversaoUtils.converterParaData(this.data);
			// Avanço de dia feito por calendário na zona canônica do domínio.
			LocalDate dataLocalReferencia = Instant.ofEpochMilli(dataReferencia.getTime())
					.atZone(BolaoTime.getZoneId())
					.toLocalDate();
			LocalDate diaSeguinteLocal = dataLocalReferencia.plusDays(1);
			Date diaSeguinte = Date.from(diaSeguinteLocal.atStartOfDay(BolaoTime.getZoneId()).toInstant());

			Date proximaDataDisponivel = getJogoService().buscarPrimeiraDataComJogosApos(diaSeguinte);

			if (proximaDataDisponivel != null) {
				FiltroBuscaJogos novoFiltro = new FiltroBuscaJogos();
				novoFiltro.setDataInicial(proximaDataDisponivel);
				novoFiltro.setDataFinal(proximaDataDisponivel);

				this.jogos = getJogoService().buscarUsandoFiltro(novoFiltro);
				this.equipes = getEquipeService().buscarApenasPaisesReais();
				LOGGER.info("[HTMX-ADMIN][LOAD-MORE] Encontrados {} jogos para a data {}", this.jogos.size(), proximaDataDisponivel);
			} else {
				this.jogos = Collections.emptyList();
				LOGGER.info("[HTMX-ADMIN][LOAD-MORE] Nenhuma data futura com jogos encontrada.");
			}
		} catch (Exception e) {
			LOGGER.error("[HTMX-ADMIN][LOAD-MORE] Erro ao buscar mais jogos via HTMX", e);
			this.jogos = Collections.emptyList();
		}

		return SUCCESS;
	}

	/**
	 * Regra padrão da tela administrativa de resultados:
	 * 1) Exibir jogos desde o início da Copa até "hoje" (São Paulo);
	 * 2) Manter opção explícita para listar calendário completo (`mostrarTodos=true`).
	 */
	private void carregarJogosComFiltroPadraoAteHoje() {
		if (this.mostrarTodos) {
			this.jogos = getJogoService().buscarTodos();
			markAdminFiltroContext(false, null, true);
			return;
		}

		Date hoje = Date.from(LocalDate.now(BolaoTime.getZoneId())
				.atStartOfDay(BolaoTime.getZoneId()).toInstant());
		FiltroBuscaJogos filtroAteHoje = new FiltroBuscaJogos();
		filtroAteHoje.setDataFinal(hoje);
		this.jogos = getJogoService().buscarUsandoFiltro(filtroAteHoje);
		markAdminFiltroContext(true, hoje, false);
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

	public Jogo getJogo() {
		return jogo;
	}

	public Long getId() {
		return id;
	}

	@StrutsParameter
	public void setId(Long id) {
		LOGGER.info("[HTMX-TRACE] setId chamado com valor: {}", id);
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

	@StrutsParameter
	public void setMostrarTodos(boolean mostrarTodos) {
		this.mostrarTodos = mostrarTodos;
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

	private void markAdminResultadoView() {
		HttpServletRequest request = getHttpRequest();
		if (request != null) {
			request.setAttribute(ADMIN_RESULTADO_VIEW_ATTRIBUTE, Boolean.TRUE);
		}
	}

	private void markAdminFiltroContext(boolean filtroAteHojeAtivo, Date dataLimite, boolean mostrandoTodos) {
		HttpServletRequest request = getHttpRequest();
		if (request == null) {
			return;
		}
		request.setAttribute("adminFiltroAteHojeAtivo", Boolean.valueOf(filtroAteHojeAtivo));
		request.setAttribute("adminMostrandoTodos", Boolean.valueOf(mostrandoTodos));
		request.setAttribute("adminFiltroDataLimite", dataLimite);
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
