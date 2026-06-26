package com.opendev.bolao.action;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.Time;
import java.security.Principal;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
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
	private static final Set<Integer> FASES_FILTRO_PERMITIDAS = new HashSet<>(java.util.Arrays.asList(11, 12, 13, 16, 8, 4, 2, 3, 1));
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
	private FiltroBuscaJogos filtro;
	private boolean usarFiltro;
	private String dataInicial;
	private String dataFinal;
	private Integer filtroFase;
	private Long filtroEquipe;
	private String filtroGrupo;
	private boolean filtroJogosNaoOcorreram;
	private List<String> filtroAvisos = new ArrayList<>();
	private String adminDeleteErrorMessage;
	private Map<Long, Boolean> elegibilidadeExclusaoPorJogo = Collections.emptyMap();

	
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
        validarAtualizacaoResultadoPermitida(idJogo);
		getJogoService().atualizarResultado(idJogo, golsEquipe1, golsEquipe2);
	}

    private void validarAtualizacaoResultadoPermitida(Long idJogo) {
        Jogo jogo = getJogoService().buscarPorId(idJogo)
                .orElseThrow(() -> new com.opendev.bolao.exception.BusinessException("Jogo não encontrado"));
        if (!jogo.jaOcorreu()) {
            throw new com.opendev.bolao.exception.BusinessException(
                    "Atualização de placar permitida apenas a partir do início do jogo.");
        }
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

	public String excluirJogoHtmx() {
		HttpServletRequest request = getHttpRequest();
		HttpServletResponse response = getHttpResponse();
		markSkipTemplate();
		markAdminResultadoView();
		if (request == null || response == null) {
			return NONE;
		}
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			LOGGER.warn("[ADMIN][EXCLUIR-JOGO] método HTTP inválido: {}", request.getMethod());
			return NONE;
		}
		if (this.id == null || this.id.longValue() <= 0L) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			LOGGER.warn("[ADMIN][EXCLUIR-JOGO] ID inválido para exclusão: {}", this.id);
			return NONE;
		}

		String operador = obterOperadorAuditoria(request);
		try {
			getJogoService().apagarJogoAdministrativo(this.id, operador);
			// HTMX nao executa swap em respostas 204 (No Content).
			// Para manter hx-swap="delete" funcional, retornamos 200.
			response.setStatus(HttpServletResponse.SC_OK);
			LOGGER.info("[ADMIN][EXCLUIR-JOGO] operador={} jogoId={} resultado=SUCCESS", operador, this.id);
			return NONE;
		} catch (com.opendev.bolao.exception.BusinessException ex) {
			int status = mapearStatusExclusao(ex);
			this.adminDeleteErrorMessage = ex.getMessage();
			response.setStatus(status);
			LOGGER.warn("[ADMIN][EXCLUIR-JOGO] operador={} jogoId={} resultado=DENY status={} motivo={}",
					operador, this.id, status, ex.getMessage());
			return NONE;
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.error("[ADMIN][EXCLUIR-JOGO] operador={} jogoId={} resultado=ERROR", operador, this.id, ex);
			return NONE;
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
			aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
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
			aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
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
			aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
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
		carregarJogosComFiltroAdmin();
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

			FiltroBuscaJogos filtroAdminAtivo = obterFiltroAdmin();
			filtroAdminAtivo = normalizarFiltroIncrementalAdmin(dataReferencia, filtroAdminAtivo);
			Date proximaDataDisponivel = buscarProximaDataDisponivelComFiltro(diaSeguinte, filtroAdminAtivo);

			if (proximaDataDisponivel != null) {
				FiltroBuscaJogos novoFiltro = montarFiltroDoDiaComRestricoes(proximaDataDisponivel, filtroAdminAtivo);
				this.filtro = novoFiltro;
				this.jogos = getJogoService().buscarUsandoFiltro(novoFiltro);
				aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
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

	private FiltroBuscaJogos normalizarFiltroIncrementalAdmin(Date dataReferencia, FiltroBuscaJogos filtroAdminAtivo) {
		if (filtroAdminAtivo == null || dataReferencia == null) {
			return filtroAdminAtivo;
		}
		if (isUsarFiltro()) {
			return filtroAdminAtivo;
		}
		boolean possuiRestricoesExplicitas = filtroAdminAtivo.getFase() != null
				|| filtroAdminAtivo.getIdEquipe() != null
				|| !ValidacaoUtils.isVazia(filtroAdminAtivo.getGrupo())
				|| filtroAdminAtivo.isSoJogosQueNaoOcorreram();
		if (possuiRestricoesExplicitas) {
			return filtroAdminAtivo;
		}
		if (!isMesmoDiaNoFusoBolao(filtroAdminAtivo.getDataInicial(), dataReferencia)
				|| !isMesmoDiaNoFusoBolao(filtroAdminAtivo.getDataFinal(), dataReferencia)) {
			return filtroAdminAtivo;
		}
		LOGGER.info("[HTMX-ADMIN][LOAD-MORE] Ignorando período implícito da carga padrão para permitir avanço incremental.");
		return null;
	}

	/**
	 * Regra padrão da tela administrativa de resultados:
	 * 1) Exibir jogos da data atual (São Paulo), quando não houver filtro explícito;
	 * 2) Manter opção explícita para listar calendário completo (`mostrarTodos=true`);
	 * 3) Aplicar filtro informado manualmente quando existir.
	 */
	private void carregarJogosComFiltroAdmin() {
		if (this.mostrarTodos) {
			this.filtro = null;
			this.jogos = getJogoService().buscarTodos();
			aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
			markAdminFiltroContext(false, null, true);
			return;
		}

		FiltroBuscaJogos filtroAdmin = obterFiltroAdmin();
		if (filtroAdmin != null) {
			this.filtro = filtroAdmin;
			this.jogos = getJogoService().buscarUsandoFiltro(filtroAdmin);
			aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
			markAdminFiltroContext(false, null, false);
			return;
		}

		Date hoje = Date.from(LocalDate.now(BolaoTime.getZoneId())
				.atStartOfDay(BolaoTime.getZoneId()).toInstant());
		FiltroBuscaJogos filtroDataAtual = new FiltroBuscaJogos();
		filtroDataAtual.setDataInicial(hoje);
		filtroDataAtual.setDataFinal(hoje);
		this.filtro = filtroDataAtual;
		this.jogos = getJogoService().buscarUsandoFiltro(filtroDataAtual);
		aplicarElegibilidadeCanonicaExclusaoAdmin(this.jogos);
		markAdminFiltroContext(true, hoje, false);
	}

	private void aplicarElegibilidadeCanonicaExclusaoAdmin(List jogosLista) {
		if (jogosLista == null || jogosLista.isEmpty()) {
			this.elegibilidadeExclusaoPorJogo = Collections.emptyMap();
			return;
		}
		List<Jogo> jogosTipados = new ArrayList<>();
		for (Object item : jogosLista) {
			if (item instanceof Jogo jogoItem) {
				jogosTipados.add(jogoItem);
			}
		}
		if (jogosTipados.isEmpty()) {
			this.elegibilidadeExclusaoPorJogo = Collections.emptyMap();
			return;
		}
		Map<Long, Boolean> resultado = getJogoService().mapearElegibilidadeExclusaoAdministrativa(jogosTipados);
		this.elegibilidadeExclusaoPorJogo = resultado == null ? Collections.emptyMap() : resultado;
	}

	private FiltroBuscaJogos obterFiltroAdmin() {
		this.filtroAvisos = new ArrayList<>();
		if (!isFiltroAdminSolicitado()) {
			return null;
		}

		FiltroBuscaJogos filtroAdmin = new FiltroBuscaJogos();
		Date dataInicialFiltro = converterDataFiltroOuNulo(getDataInicial(), "dataInicial");
		Date dataFinalFiltro = converterDataFiltroOuNulo(getDataFinal(), "dataFinal");
		if (dataInicialFiltro != null && dataFinalFiltro != null && dataFinalFiltro.before(dataInicialFiltro)) {
			LOGGER.warn("[FILTRO][ADMIN][JOGOS] intervalo invertido detectado; aplicando swap dataInicial={} dataFinal={}",
					getDataInicial(), getDataFinal());
			this.filtroAvisos.add("O intervalo de datas estava invertido e foi ajustado automaticamente.");
			Date tmp = dataInicialFiltro;
			dataInicialFiltro = dataFinalFiltro;
			dataFinalFiltro = tmp;
		}

		filtroAdmin.setDataInicial(dataInicialFiltro);
		filtroAdmin.setDataFinal(dataFinalFiltro);

		if (getFiltroFase() != null) {
			if (FASES_FILTRO_PERMITIDAS.contains(getFiltroFase())) {
				filtroAdmin.setFase(getFiltroFase());
			} else {
				LOGGER.warn("[FILTRO][ADMIN][JOGOS] fase fora da whitelist ignorada: {}", getFiltroFase());
				this.filtroAvisos.add("A fase selecionada é inválida e foi ignorada.");
			}
		}

		if (getFiltroEquipe() != null) {
			if (isEquipeFiltroPermitida(getFiltroEquipe())) {
				filtroAdmin.setIdEquipe(getFiltroEquipe());
			} else {
				LOGGER.warn("[FILTRO][ADMIN][JOGOS] equipe fora da lista permitida ignorada: {}", getFiltroEquipe());
				this.filtroAvisos.add("A equipe selecionada não é válida para este filtro e foi ignorada.");
			}
		}

		String grupoCanonico = normalizarGrupoFiltro(getFiltroGrupo());
		if (!ValidacaoUtils.isVazia(grupoCanonico)) {
			filtroAdmin.setGrupo(grupoCanonico);
		} else if (!ValidacaoUtils.isVazia(getFiltroGrupo())) {
			LOGGER.warn("[FILTRO][ADMIN][JOGOS] grupo inválido ignorado: '{}'", getFiltroGrupo());
			this.filtroAvisos.add("O grupo informado é inválido e foi ignorado.");
		}

		filtroAdmin.setSoJogosQueNaoOcorreram(isFiltroJogosNaoOcorreram());
		return filtroAdmin;
	}

	private Date converterDataFiltroOuNulo(String dataTexto, String campo) {
		if (ValidacaoUtils.isVazia(dataTexto)) {
			return null;
		}
		Date dataConvertida = ConversaoUtils.converterParaData(dataTexto);
		boolean invalida = dataConvertida == null
				|| !dataTexto.equals(ConversaoUtils.converterParaString(dataConvertida));
		if (invalida) {
			LOGGER.warn("[FILTRO][ADMIN][JOGOS] {} inválida ignorada: '{}'", campo, dataTexto);
			if ("dataInicial".equals(campo)) {
				this.filtroAvisos.add("A data inicial informada é inválida e foi ignorada.");
			} else {
				this.filtroAvisos.add("A data final informada é inválida e foi ignorada.");
			}
			return null;
		}
		return dataConvertida;
	}

	private boolean isFiltroAdminSolicitado() {
		return isUsarFiltro()
				|| !ValidacaoUtils.isVazia(getDataInicial())
				|| !ValidacaoUtils.isVazia(getDataFinal())
				|| getFiltroFase() != null
				|| getFiltroEquipe() != null
				|| !ValidacaoUtils.isVazia(getFiltroGrupo())
				|| isFiltroJogosNaoOcorreram();
	}

	private boolean isEquipeFiltroPermitida(Long equipeId) {
		if (equipeId == null || getEquipeService() == null) {
			return false;
		}
		List equipesPermitidas = getEquipeService().buscarApenasPaisesReais();
		if (equipesPermitidas == null) {
			return false;
		}
		for (Object item : equipesPermitidas) {
			if (item instanceof com.opendev.bolao.model.Equipe equipe
					&& equipe.getId() != null
					&& equipe.getId().equals(equipeId)) {
				return true;
			}
		}
		return false;
	}

	private String normalizarGrupoFiltro(String grupo) {
		if (ValidacaoUtils.isVazia(grupo)) {
			return null;
		}
		String canonico = grupo.trim().toUpperCase();
		if (canonico.matches("^[A-L]$")) {
			return canonico;
		}
		return null;
	}

	private Date buscarProximaDataDisponivelComFiltro(Date dataInicioBusca, FiltroBuscaJogos filtroBase) {
		Date cursor = dataInicioBusca;
		for (int tentativa = 0; tentativa < 366; tentativa++) {
			Date proximaDataDisponivel = getJogoService().buscarPrimeiraDataComJogosApos(cursor);
			if (proximaDataDisponivel == null) {
				return null;
			}
			if (filtroBase != null && excedeDataFinal(proximaDataDisponivel, filtroBase.getDataFinal())) {
				return null;
			}
			if (filtroBase == null) {
				return proximaDataDisponivel;
			}
			FiltroBuscaJogos filtroDia = montarFiltroDoDiaComRestricoes(proximaDataDisponivel, filtroBase);
			List jogosDaData = getJogoService().buscarUsandoFiltro(filtroDia);
			if (jogosDaData != null && !jogosDaData.isEmpty()) {
				return proximaDataDisponivel;
			}

			LocalDate dataLocal = Instant.ofEpochMilli(proximaDataDisponivel.getTime())
					.atZone(BolaoTime.getZoneId())
					.toLocalDate();
			cursor = Date.from(dataLocal.plusDays(1).atStartOfDay(BolaoTime.getZoneId()).toInstant());
		}
		return null;
	}

	private boolean excedeDataFinal(Date dataCandidata, Date dataFinalLimite) {
		if (dataCandidata == null || dataFinalLimite == null) {
			return false;
		}
		LocalDate candidataLocal = Instant.ofEpochMilli(dataCandidata.getTime())
				.atZone(BolaoTime.getZoneId())
				.toLocalDate();
		LocalDate limiteLocal = Instant.ofEpochMilli(dataFinalLimite.getTime())
				.atZone(BolaoTime.getZoneId())
				.toLocalDate();
		return candidataLocal.isAfter(limiteLocal);
	}

	private boolean isMesmoDiaNoFusoBolao(Date primeiraData, Date segundaData) {
		if (primeiraData == null || segundaData == null) {
			return false;
		}
		LocalDate primeiroDia = Instant.ofEpochMilli(primeiraData.getTime())
				.atZone(BolaoTime.getZoneId())
				.toLocalDate();
		LocalDate segundoDia = Instant.ofEpochMilli(segundaData.getTime())
				.atZone(BolaoTime.getZoneId())
				.toLocalDate();
		return primeiroDia.equals(segundoDia);
	}

	private FiltroBuscaJogos montarFiltroDoDiaComRestricoes(Date dia, FiltroBuscaJogos filtroBase) {
		FiltroBuscaJogos filtroDia = new FiltroBuscaJogos();
		filtroDia.setDataInicial(dia);
		filtroDia.setDataFinal(dia);
		if (filtroBase == null) {
			return filtroDia;
		}
		filtroDia.setFase(filtroBase.getFase());
		filtroDia.setIdEquipe(filtroBase.getIdEquipe());
		filtroDia.setGrupo(filtroBase.getGrupo());
		filtroDia.setSoJogosQueNaoOcorreram(filtroBase.isSoJogosQueNaoOcorreram());
		return filtroDia;
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

	public boolean isUsarFiltro() {
		return usarFiltro;
	}

	@StrutsParameter
	public void setUsarFiltro(boolean usarFiltro) {
		this.usarFiltro = usarFiltro;
	}

	public String getDataInicial() {
		return dataInicial;
	}

	@StrutsParameter
	public void setDataInicial(String dataInicial) {
		this.dataInicial = SanitizationUtils.cleanText(dataInicial, 16);
	}

	public String getDataFinal() {
		return dataFinal;
	}

	@StrutsParameter
	public void setDataFinal(String dataFinal) {
		this.dataFinal = SanitizationUtils.cleanText(dataFinal, 16);
	}

	public Integer getFiltroFase() {
		return filtroFase;
	}

	@StrutsParameter
	public void setFiltroFase(Integer filtroFase) {
		this.filtroFase = filtroFase;
	}

	public Long getFiltroEquipe() {
		return filtroEquipe;
	}

	@StrutsParameter
	public void setFiltroEquipe(Long filtroEquipe) {
		this.filtroEquipe = filtroEquipe;
	}

	public String getFiltroGrupo() {
		return filtroGrupo;
	}

	@StrutsParameter
	public void setFiltroGrupo(String filtroGrupo) {
		this.filtroGrupo = SanitizationUtils.cleanText(filtroGrupo, 5);
	}

	public boolean isFiltroJogosNaoOcorreram() {
		return filtroJogosNaoOcorreram;
	}

	@StrutsParameter
	public void setFiltroJogosNaoOcorreram(boolean filtroJogosNaoOcorreram) {
		this.filtroJogosNaoOcorreram = filtroJogosNaoOcorreram;
	}

	public FiltroBuscaJogos getFiltro() {
		return filtro;
	}

	public List<String> getFiltroAvisos() {
		return filtroAvisos;
	}

	public String getAdminDeleteErrorMessage() {
		return adminDeleteErrorMessage;
	}

	public Map<Long, Boolean> getElegibilidadeExclusaoPorJogo() {
		return this.elegibilidadeExclusaoPorJogo;
	}

	private int mapearStatusExclusao(com.opendev.bolao.exception.BusinessException ex) {
		if (ex == null || ex.getCode() == null) {
			return HttpServletResponse.SC_CONFLICT;
		}
		if (ex.getCode() == com.opendev.bolao.exception.BusinessException.Code.NOT_FOUND) {
			return HttpServletResponse.SC_NOT_FOUND;
		}
		if (ex.getCode() == com.opendev.bolao.exception.BusinessException.Code.INVALID_INPUT) {
			return HttpServletResponse.SC_BAD_REQUEST;
		}
		if (ex.getCode() == com.opendev.bolao.exception.BusinessException.Code.CONFLICT) {
			return HttpServletResponse.SC_CONFLICT;
		}
		if (ex.getCode() == com.opendev.bolao.exception.BusinessException.Code.DELETE_NOT_ALLOWED) {
			return HttpServletResponse.SC_CONFLICT;
		}
		return HttpServletResponse.SC_CONFLICT;
	}

	private String obterOperadorAuditoria(HttpServletRequest request) {
		if (request == null) {
			return "desconhecido";
		}
		Principal principal = request.getUserPrincipal();
		if (principal == null || ValidacaoUtils.isVazia(principal.getName())) {
			return "desconhecido";
		}
		return SanitizationUtils.cleanText(principal.getName(), 64);
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
