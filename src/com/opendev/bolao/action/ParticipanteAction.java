package com.opendev.bolao.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteAuthorizationService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.service.dto.PalpiteAuthorization;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.MensagemErro;
import com.opendev.bolao.util.FiltroBuscaJogos;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipanteAction extends ActionSupport {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ParticipanteAction.class);
	private static final String LOG_PREFIX_UPDATE = "[HTMX][UPDATE]";
	private static final String LOG_PREFIX_PREPARE = "[HTMX][PREP]";

	private PalpiteService palpiteService;
    private ParticipanteService participanteService;
    private JogoService jogoService;
    private EquipeService equipeService;
    private boolean telaPalpites;
    private List jogos;
    private List palpites;
    private List errosInclusao;
    private Participante tentativaInclusao;
    private boolean sucessoCadastro;
    private List participantes;
    private List equipes;
    private FiltroBuscaJogos filtro;
    private Participante participanteLogado;
    private long progressoPalpitesTotal;
    private long progressoPalpitesPreenchidos;
    private int progressoPalpitesPercentual;
    private Map<Long, Palpite> palpitesUsuario;
    private Long jogoId;
    private Integer palpiteGolsEquipe1;
    private Integer palpiteGolsEquipe2;
    private boolean palpiteAtualizado;
    private String palpiteErro;
    private Jogo jogoSelecionado;
    private Palpite palpiteSelecionado;
    private boolean palpitePermitido;
    private String palpiteStatus = "locked";
    private String palpiteBloqueioMotivo;
    private PalpiteAuthorizationService palpiteAuthorizationService;
    
    // Dados página principal
    private List jogosDeHoje;
    private InputStream graficoStream;
    private String rival;

    // Parâmetros de Cadastro / Senha
    private String login;
    private String nome;
    private String email;
    private String senha;
    private String senhaAtual;
    private String novaSenha;
    private String confirmarSenha;

    // Parâmetros de Filtro
    private boolean usarFiltro;
    private String dataInicial;
    private String dataFinal;
    private Integer filtroFase;
    private Long filtroEquipe;
    private String filtroGrupo;
    private boolean filtroSemPalpite;
    private boolean filtroJogosNaoOcorreram;
    private boolean loginPossuiHtml;
    private boolean nomePossuiHtml;
    private boolean emailPossuiHtml;

    public String index() {
        return SUCCESS;
    }

    public String login() {
        return SUCCESS;
    }

    public String cadastroForm() {
        return SUCCESS;
    }

    public String regras() {
        return SUCCESS;
    }

    public String batePapo() {
        return SUCCESS;
    }

    public String trocaSenha() {
        LOGGER.info("[PERFIL][SENHA] Acessando formulario de troca de senha.");
        return SUCCESS;
    }

    public String alterarSenha() {
        LOGGER.info("[PERFIL][SENHA] Recebendo solicitacao de alteracao de senha.");
        String loginLocal = RequestUtils.getLoginParticipanteAutenticado();
        if (loginLocal == null) {
            return LOGIN;
        }

        if (ValidacaoUtils.isVazia(senhaAtual) || ValidacaoUtils.isVazia(novaSenha) || ValidacaoUtils.isVazia(confirmarSenha)) {
            addActionError(getText("match.submit.error", "Todos os campos são obrigatórios."));
            return INPUT;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            addActionError(getText("pwd.change.error.mismatch", "As senhas informadas não conferem."));
            return INPUT;
        }

        try {
            getParticipanteService().alterarSenha(loginLocal, senhaAtual, novaSenha);
            addActionMessage(getText("pwd.change.success", "Senha alterada com sucesso!"));
            // Limpa campos após sucesso
            this.senhaAtual = null;
            this.novaSenha = null;
            this.confirmarSenha = null;
            return SUCCESS;
        } catch (ValidacaoException e) {
            if (e.getErros() != null && !e.getErros().isEmpty()) {
                MensagemErro erro = (MensagemErro) e.getErros().get(0);
                addActionError(erro.getMensagem());
            } else {
                addActionError(e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("[PERFIL][SENHA] Erro inesperado ao trocar senha para usuario={}", loginLocal, e);
            addActionError("Erro inesperado ao processar a troca de senha.");
        }

        return INPUT;
    }

	public String logout() {
		HttpSession session = RequestUtils.getRequest().getSession();
		session.invalidate();
		return SUCCESS;
	}
	
	public String prepararInfoPalpites() {
	       if (this.login == null) {
	           this.login = RequestUtils.getLoginParticipanteAutenticado();
	       }
	       FiltroBuscaJogos filtro = obterFiltro();
	       if (filtro == null) {
	           setJogos(getJogoService().buscarTodos());
	       } else {
	           setJogos(getJogoService().buscarUsandoFiltro(filtro));
	           setFiltro(filtro);
	       }
	       setEquipes(getEquipeService().buscarApenasPaisesReais());
	       setTelaPalpites(true);
	       prepararMapaPalpitesUsuario();
	       atualizarProgressoPalpites(filtro);
	       return SUCCESS;
	   }

    private void prepararMapaPalpitesUsuario() {
        String loginLocal = this.login;
        if (loginLocal == null) {
            loginLocal = RequestUtils.getLoginParticipanteAutenticado();
        }
        
        if (loginLocal == null) {
            LOGGER.warn("prepararMapaPalpitesUsuario: login nao identificado, mapa ficara vazio.");
            return;
        }

        List palpitesDoUsuario = getPalpiteService().buscarPalpitesDoParticipante(loginLocal);
        if (palpitesDoUsuario == null) {
            return;
        }
        Map<Long, Palpite> mapa = new HashMap<>();
        for (Object objeto : palpitesDoUsuario) {
            Palpite palpite = (Palpite) objeto;
            if (palpite.getJogo() != null && palpite.getJogo().getId() != null) {
                mapa.put(palpite.getJogo().getId(), palpite);
            }
        }
        setPalpitesUsuario(mapa);
    }
	
    public String gerarGraficoDesempenho() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        Participante participante = getParticipanteService().buscarPorLogin(login).orElse(null);
        setParticipanteLogado(participante);
        List participantes = getParticipanteService().buscarClassificacao();
        participantes.remove(participante);
        Collections.sort(participantes, Participante.COMPARADOR_NOME);
        setParticipantes(participantes);
        return SUCCESS;
    }

    public String gerarGraficoDesempenhoImagem() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        Participante participante = getParticipanteService().buscarPorLogin(login).orElse(null);
        Long idRival = obterIdRival();
        GraficoComparativoDesempenho grafico = getParticipanteService().construirGraficoDesempenho(participante, idRival);
        JFreeChart chart = grafico.criarChart();
        this.graficoStream = renderizarChart(chart, 560, 240);
        return SUCCESS;
    }
    
    public String obterDadosPaginaPrincipal() {
        setJogosDeHoje(getJogoService().buscarJogosDeHoje());
        return SUCCESS;
    }

    public String gerarGraficoLiderancaImagem() {
        GraficoBarraLideres grafico = getParticipanteService().construirGraficoDeBarrasDosLideres();
        JFreeChart chart = grafico.criarChart();
        this.graficoStream = renderizarChart(chart, 560, 180);
        return SUCCESS;
    }

    private Long obterIdRival() {
        if (ValidacaoUtils.isVazia(this.rival)) {
            return null;
        }
        try {
            return Long.valueOf(this.rival);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private InputStream renderizarChart(JFreeChart chart, int largura, int altura) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(buffer, chart, largura, altura);
            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao gerar grafico.", ex);
        }
    }

	private FiltroBuscaJogos obterFiltro() {
		FiltroBuscaJogos filtro = null;
        if (isUsarFiltro()) {
            filtro = new FiltroBuscaJogos();
    		filtro.setDataInicial(ConversaoUtils.converterParaData(getDataInicial()));
    		filtro.setDataFinal(ConversaoUtils.converterParaData(getDataFinal()));
            if (getFiltroFase() != null) {
                filtro.setFase(getFiltroFase());
            }
            if (getFiltroEquipe() != null) {
                filtro.setIdEquipe(getFiltroEquipe());
            }
    		filtro.setGrupo(getFiltroGrupo());
            filtro.setSoSemPalpite(isFiltroSemPalpite());
            filtro.setSoJogosQueNaoOcorreram(isFiltroJogosNaoOcorreram());
            filtro.setLogin(RequestUtils.getLoginParticipanteAutenticado());
        }
		return filtro;
	}
    
    private Participante obterParticipante() {
        Participante p = new Participante();
        p.setLogin(getLogin());
        p.setNome(getNome());
        p.setEmail(getEmail());
        p.setSenha(getSenha());
        p.setIp(RequestUtils.getIpDaRequisicao());
        return p;
    }

    // Getters e Setters anotados para Struts 7

    public String getLogin() { return login; }
    @StrutsParameter
    public void setLogin(String login) {
        this.loginPossuiHtml = SanitizationUtils.containsHtml(login);
        this.login = SanitizationUtils.cleanText(login, 32);
    }

    public String getNome() { return nome; }
    @StrutsParameter
    public void setNome(String nome) {
        this.nomePossuiHtml = SanitizationUtils.containsHtml(nome);
        this.nome = SanitizationUtils.cleanText(nome, 80);
    }

    public String getEmail() { return email; }
    @StrutsParameter
    public void setEmail(String email) {
        this.emailPossuiHtml = SanitizationUtils.containsHtml(email);
        this.email = SanitizationUtils.cleanText(email, 254);
    }

    public String getSenha() { return senha; }
    @StrutsParameter
    public void setSenha(String senha) {
        this.senha = senha == null ? null : senha.trim();
    }

    public String getSenhaAtual() { return senhaAtual; }
    @StrutsParameter
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

    public String getNovaSenha() { return novaSenha; }
    @StrutsParameter
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }

    public String getConfirmarSenha() { return confirmarSenha; }
    @StrutsParameter
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }

    public boolean isUsarFiltro() { return usarFiltro; }
    @StrutsParameter
    public void setUsarFiltro(boolean usarFiltro) { this.usarFiltro = usarFiltro; }

    public String getDataInicial() { return dataInicial; }
    @StrutsParameter
    public void setDataInicial(String dataInicial) { this.dataInicial = dataInicial; }

    public String getDataFinal() { return dataFinal; }
    @StrutsParameter
    public void setDataFinal(String dataFinal) { this.dataFinal = dataFinal; }

    public Integer getFiltroFase() { return filtroFase; }
    @StrutsParameter
    public void setFiltroFase(Integer filtroFase) { this.filtroFase = filtroFase; }

    public Long getFiltroEquipe() { return filtroEquipe; }
    @StrutsParameter
    public void setFiltroEquipe(Long filtroEquipe) { this.filtroEquipe = filtroEquipe; }

    public String getFiltroGrupo() { return filtroGrupo; }
    @StrutsParameter
    public void setFiltroGrupo(String filtroGrupo) {
        this.filtroGrupo = SanitizationUtils.cleanText(filtroGrupo, 5);
    }

    public boolean isFiltroSemPalpite() { return filtroSemPalpite; }
    @StrutsParameter
    public void setFiltroSemPalpite(boolean filtroSemPalpite) { this.filtroSemPalpite = filtroSemPalpite; }

    public boolean isFiltroJogosNaoOcorreram() { return filtroJogosNaoOcorreram; }
    @StrutsParameter
    public void setFiltroJogosNaoOcorreram(boolean filtroJogosNaoOcorreram) { this.filtroJogosNaoOcorreram = filtroJogosNaoOcorreram; }
    
    public List buscarMeusPalpites() {
    	String login = RequestUtils.getLoginParticipanteAutenticado();
    	return getPalpiteService().buscarPalpitesDoParticipante(login);
    }
    
    public String listarPalpitesDoJogoHtmx() {
        marcarRespostaParcial();
        if (this.jogoId == null) {
            this.palpites = Collections.emptyList();
            return SUCCESS;
        }
        this.palpites = getPalpiteService().buscarPalpitesDoJogo(this.jogoId);
        return SUCCESS;
    }

    public String carregarPalpiteFormHtmx() {
        marcarRespostaParcial();
        prepararConteudoPalpite();
        if (this.jogoSelecionado == null) {
            this.palpiteAtualizado = false;
            return ERROR;
        }
        this.palpiteAtualizado = false;
        return SUCCESS;
    }

    public String atualizarPalpiteHtmx() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = RequestUtils.getRequest();
        String ip = RequestUtils.getIpDaRequisicao();
        StringBuilder paramLog = new StringBuilder();
        paramLog.append(" [PARAMS] jogoId=").append(this.jogoId);
        paramLog.append(", gols1=").append(this.palpiteGolsEquipe1);
        paramLog.append(", gols2=").append(this.palpiteGolsEquipe2);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} inicio login={}{}", LOG_PREFIX_UPDATE, login, paramLog);
        }
        
        if (request != null && request.getCookies() != null) {
            StringBuilder cookiesBuilder = new StringBuilder();
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookiesBuilder.length() > 0) {
                    cookiesBuilder.append("; ");
                }
                cookiesBuilder.append(cookie.getName()).append('=').append(cookie.getValue());
            }
            LOGGER.debug("atualizarPalpiteHtmx: cookies={}", cookiesBuilder);
        }
        if (login == null) {
            LOGGER.warn("atualizarPalpiteHtmx: usuario nao autenticado, abortando.");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{} resultado=ERROR motivo=usuarioNaoAutenticado jogoId={} gols1={} gols2={}",
                        LOG_PREFIX_UPDATE, this.jogoId, this.palpiteGolsEquipe1, this.palpiteGolsEquipe2);
            }
            this.palpiteAtualizado = false;
            this.palpiteErro = getText("match.tip.error.unavailable");
            if (this.jogoId != null) {
                prepararConteudoPalpite();
            }
            atualizarProgressoPalpites(obterFiltro());
            return ERROR;
        }
        // Validação básica: precisamos ao menos do jogoId
        if (this.jogoId == null) {
            LOGGER.warn("{} jogoId ausente", LOG_PREFIX_UPDATE);
            this.palpiteAtualizado = false;
            this.palpiteErro = getText("match.tip.error.unavailable");
            atualizarProgressoPalpites(obterFiltro());
            return ERROR;
        }

        // Criar palpite temporário para preservar o que o usuário digitou, mesmo que não salvemos ainda
        Palpite palpiteTemp = new Palpite();
        palpiteTemp.setIdJogo(this.jogoId);
        palpiteTemp.setGolsEquipe1(this.palpiteGolsEquipe1);
        palpiteTemp.setGolsEquipe2(this.palpiteGolsEquipe2);
        this.palpiteSelecionado = palpiteTemp;

        // Só tentamos salvar se ambos os gols estiverem presentes
        if (this.palpiteGolsEquipe1 == null || this.palpiteGolsEquipe2 == null) {
            LOGGER.debug("{} salvamento postergado: aguardando preenchimento de ambos os campos (gols1={}, gols2={})",
                    LOG_PREFIX_UPDATE, this.palpiteGolsEquipe1, this.palpiteGolsEquipe2);
            this.palpiteAtualizado = false;
            prepararConteudoPalpite();
            // Sobrescrevemos o palpite carregado do banco pelo temporário para manter o valor no input
            this.palpiteSelecionado = palpiteTemp;
            atualizarProgressoPalpites(obterFiltro());
            return SUCCESS;
        }
        String resultado;
        try {
            getPalpiteService().atualizarPalpite(login, this.jogoId, this.palpiteGolsEquipe1, this.palpiteGolsEquipe2, ip);
            this.palpiteAtualizado = true;
            prepararMapaPalpitesUsuario();
            prepararConteudoPalpite();
            resultado = SUCCESS;
        } catch (Exception ex) {
            LOGGER.error("atualizarPalpiteHtmx: falha ao atualizar palpite (login={}, jogoId={})", login, this.jogoId, ex);
            this.palpiteAtualizado = false;
            if (ex instanceof IllegalStateException) {
                this.palpiteErro = getText("match.tip.locked.timeWindow");
            } else {
                this.palpiteErro = getText("match.tip.error.unavailable");
            }
            prepararConteudoPalpite();
            resultado = ERROR;
        }
        atualizarProgressoPalpites(obterFiltro());
        if (SUCCESS.equals(resultado) && this.palpiteAtualizado && request != null
                && "true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            HttpServletResponse response = ServletActionContext.getResponse();
            if (response != null) {
                response.setHeader("HX-Trigger", "{\"palpiteProgressRefresh\":true}");
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} resultado={} palpiteAtualizado={} palpiteStatus={} palpitePermitido={} palpiteErro={}",
                    LOG_PREFIX_UPDATE,
                    resultado,
                    this.palpiteAtualizado,
                    this.palpiteStatus,
                    this.palpitePermitido,
                    this.palpiteErro);
        }
        return resultado;
    }

    private void prepararConteudoPalpite() {
        this.palpiteSelecionado = null;
        this.jogoSelecionado = null;
        this.palpitePermitido = false;
        this.palpiteStatus = "locked";
        this.palpiteBloqueioMotivo = null;

        if (this.jogoId == null) {
            LOGGER.debug("prepararConteudoPalpite: jogoId ausente, encerrando.");
            return;
        }

        Jogo jogo = getJogoService().buscarPorId(this.jogoId).orElse(null);
        if (jogo == null) {
            LOGGER.warn("prepararConteudoPalpite: jogo nao encontrado jogoId={}", this.jogoId);
            return;
        }
        this.jogoSelecionado = jogo;

        String login = RequestUtils.getLoginParticipanteAutenticado();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = RequestUtils.getRequest();
        
        registrarCabecalhosHtmx(request, LOG_PREFIX_PREPARE);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} jogoId={} data={} hora={} podeDarPalpite={}",
                    LOG_PREFIX_PREPARE,
                    this.jogoId,
                    jogo.getData(),
                    jogo.getHora(),
                    jogo.getPodeDarPalpite());
        }
        LOGGER.debug("prepararConteudoPalpite: login={}, authClass={}, principal={}, sessionId={}",
                login,
                authentication == null ? "null" : authentication.getClass().getName(),
                authentication == null ? "null" : authentication.getPrincipal(),
                request == null ? "null" : request.getRequestedSessionId());
        if (request != null && request.getCookies() != null) {
            StringBuilder cookiesBuilder = new StringBuilder();
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookiesBuilder.length() > 0) {
                    cookiesBuilder.append("; ");
                }
                cookiesBuilder.append(cookie.getName()).append('=').append(cookie.getValue());
            }
            LOGGER.debug("prepararConteudoPalpite: cookies={}", cookiesBuilder);
        }
        if (login != null) {
            try {
                this.palpiteSelecionado = getPalpiteService().buscarPalpiteDoJogo(login, this.jogoId);
                if (LOGGER.isInfoEnabled()) {
                    if (this.palpiteSelecionado != null) {
                        LOGGER.info("{} palpiteEncontrado=true login={} jogoId={} gols1={} gols2={} atualizadoEm={} ip={}",
                                LOG_PREFIX_PREPARE,
                                login,
                                this.jogoId,
                                this.palpiteSelecionado.getGolsEquipe1(),
                                this.palpiteSelecionado.getGolsEquipe2(),
                                this.palpiteSelecionado.getDataHoraAtualizacao(),
                                this.palpiteSelecionado.getIp());
                    } else {
                        LOGGER.info("{} palpiteEncontrado=false login={} jogoId={}", LOG_PREFIX_PREPARE, login, this.jogoId);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("prepararConteudoPalpite: erro buscando palpite do jogo (login={}, jogoId={})", login, this.jogoId, ex);
                this.palpiteSelecionado = null;
            }
        } else if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} login_nulo jogoId={} (nenhum palpite carregado)", LOG_PREFIX_PREPARE, this.jogoId);
        }

        PalpiteAuthorization authorization = avaliarAutorizacao(jogo, this.palpiteSelecionado);
        LOGGER.debug("prepararConteudoPalpite: jogoId={}, autorizacao={{permitido={}, status={}, motivo={}}}",
                this.jogoId, authorization.isPermitido(), authorization.getStatus(), authorization.getReason());
        this.palpitePermitido = authorization.isPermitido();
        this.palpiteStatus = authorization.getStatus().getKey();
        this.palpiteBloqueioMotivo = authorization.getReason().getKey();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} autorizacao login={} jogoId={} permitido={} status={} motivo={}",
                    LOG_PREFIX_PREPARE,
                    login,
                    this.jogoId,
                    this.palpitePermitido,
                    this.palpiteStatus,
                    this.palpiteBloqueioMotivo);
        }
    }

    private PalpiteAuthorization avaliarAutorizacao(Jogo jogo, Palpite palpite) {
        if (palpiteAuthorizationService != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return palpiteAuthorizationService.avaliar(authentication, jogo, palpite);
        }
        boolean possuiRole = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String nome = authority != null ? authority.getAuthority() : null;
                if (nome == null) {
                    continue;
                }
                String normalizado = nome.trim().toUpperCase();
                if (!normalizado.startsWith("ROLE_")) {
                    normalizado = "ROLE_" + normalizado;
                }
                if ("ROLE_USER".equals(normalizado) || "ROLE_ADMIN".equals(normalizado)) {
                    possuiRole = true;
                    break;
                }
            }
        }
        boolean janelaAberta = jogo != null && jogo.getPodeDarPalpite();
        boolean permitido = possuiRole && janelaAberta;
        PalpiteAuthorization.Status status;
        if (palpite != null) {
            status = PalpiteAuthorization.Status.REGISTERED;
        } else if (permitido) {
            status = PalpiteAuthorization.Status.PENDING;
        } else {
            status = PalpiteAuthorization.Status.LOCKED;
        }
        if (permitido) {
            return PalpiteAuthorization.permitido(status);
        }
        PalpiteAuthorization.RejectionReason motivo;
        if (!possuiRole) {
            motivo = PalpiteAuthorization.RejectionReason.ROLE_MISSING;
        } else if (!janelaAberta) {
            motivo = PalpiteAuthorization.RejectionReason.TIME_WINDOW;
        } else {
            motivo = PalpiteAuthorization.RejectionReason.UNKNOWN;
        }
        return PalpiteAuthorization.negado(status, motivo);
    }

    private void marcarRespostaParcial() {
        HttpServletRequest request = RequestUtils.getRequest();
        if (request != null) {
            request.setAttribute("skipTemplate", Boolean.TRUE);
        }
    }

    private void atualizarProgressoPalpites(FiltroBuscaJogos filtro) {
        if (this.palpitesUsuario == null) {
            prepararMapaPalpitesUsuario();
        }
        long totalJogos = getJogoService().contarJogosUsandoFiltro(filtro);
        this.progressoPalpitesTotal = Math.max(0, totalJogos);
        if (this.palpitesUsuario == null || this.palpitesUsuario.isEmpty() || totalJogos == 0) {
            this.progressoPalpitesPreenchidos = 0;
            this.progressoPalpitesPercentual = 0;
            return;
        }
        long preenchidos = 0;
        List jogosFiltrados = filtro == null ? getJogoService().buscarTodos() : getJogoService().buscarUsandoFiltro(filtro);
        if (jogosFiltrados != null) {
            for (Object item : jogosFiltrados) {
                Jogo jogo = (Jogo) item;
                if (jogo == null || jogo.getId() == null) {
                    continue;
                }
                Palpite palpite = this.palpitesUsuario.get(jogo.getId());
                if (palpite != null && palpite.getGolsEquipe1() != null && palpite.getGolsEquipe2() != null) {
                    preenchidos++;
                }
            }
        }
        this.progressoPalpitesPreenchidos = preenchidos;
        this.progressoPalpitesPercentual = (int) Math.min(100, Math.round((double) preenchidos * 100.0 / (double) totalJogos));
    }

    private void registrarCabecalhosHtmx(HttpServletRequest request, String prefixoLog) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        if (request == null) {
            LOGGER.info("{} headers indisponiveis (request nulo)", prefixoLog);
            return;
        }
        LOGGER.info("{} headers hx-request={}, hx-trigger={}, hx-target={}, hx-current-url={}, hx-history-restore={}, referer={}, user-agent={}",
                prefixoLog,
                request.getHeader("HX-Request"),
                request.getHeader("HX-Trigger"),
                request.getHeader("HX-Target"),
                request.getHeader("HX-Current-URL"),
                request.getHeader("HX-History-Restore-Request"),
                request.getHeader("Referer"),
                request.getHeader("User-Agent"));
    }

	public String cadastrar() {
        Participante p = obterParticipante();
        if (!validarCadastroEntradas()) {
            setTentativaInclusao(p);
            return INPUT;
        }
        if (verificarDuplicidadeCadastro(p)) {
            setTentativaInclusao(p);
            return INPUT;
        }
        try {
            getParticipanteService().criarNovo(p);
            setSucessoCadastro(true);
            return SUCCESS;
        } catch (ValidacaoException e) {
            setTentativaInclusao(p);
            setErrosInclusao(e.getErros());
        }
		return INPUT;
	}
    
    public String buscarParticipantes() {
        this.participantes = getParticipanteService().buscarClassificacao();
        Collections.sort(this.participantes);
        return SUCCESS;
    }
	
	public boolean existeLogin(String login) {
		return getParticipanteService().buscarPorLogin(login).isPresent();
	}
	
	public void atualizarPalpite(Long idJogo, Integer golsEquipe1, Integer golsEquipe2) {
		String login = RequestUtils.getLoginParticipanteAutenticado();
		String ip = RequestUtils.getIpDaRequisicao();
		getPalpiteService().atualizarPalpite(login, idJogo, golsEquipe1, golsEquipe2, ip);
	}
	
	public Palpite buscarPalpiteDoJogo(Long idJogo) {
		String login = RequestUtils.getLoginParticipanteAutenticado();
		return getPalpiteService().buscarPalpiteDoJogo(login, idJogo);
	}
    
    public List buscarPalpitesDoJogo(Long idJogo) {
        return getPalpiteService().buscarPalpitesDoJogo(idJogo);
    }
    
    public String gerarClassificacaoDaCopa() {
        return SUCCESS;
    }

	public PalpiteService getPalpiteService() {
		return palpiteService;
	}

	public void setPalpiteService(PalpiteService palpiteService) {
		this.palpiteService = palpiteService;
	}

	public JogoService getJogoService() {
		return jogoService;
	}

	public void setJogoService(JogoService jogoService) {
		this.jogoService = jogoService;
	}

    public List getJogos() {
        return jogos;
    }

    public void setJogos(List jogos) {
        this.jogos = jogos;
    }

    public Map<Long, Palpite> getPalpitesUsuario() {
        return palpitesUsuario;
    }

    public void setPalpitesUsuario(Map<Long, Palpite> palpitesUsuario) {
        this.palpitesUsuario = palpitesUsuario;
    }

	public boolean isTelaPalpites() {
		return telaPalpites;
	}

	public void setTelaPalpites(boolean telaPalpites) {
		this.telaPalpites = telaPalpites;
	}

	public ParticipanteService getParticipanteService() {
		return participanteService;
	}

    public void setParticipanteService(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    public List getParticipantes() {
        return this.participantes;
    }

    public List getPalpites() {
        return palpites;
    }

    public boolean isPalpiteAtualizado() {
        return palpiteAtualizado;
    }

    public String getPalpiteErro() {
        return palpiteErro;
    }

    public Jogo getJogoSelecionado() {
        return jogoSelecionado;
    }

    public Palpite getPalpiteSelecionado() {
        return palpiteSelecionado;
    }

    public boolean isPalpitePermitido() {
        return palpitePermitido;
    }

    public String getPalpiteStatus() {
        return palpiteStatus;
    }

    public String getPalpiteBloqueioMotivo() {
        return palpiteBloqueioMotivo;
    }

    public List getErrosInclusao() {
        return this.errosInclusao;
    }

    public void setErrosInclusao(List errosInclusao) {
        this.errosInclusao = errosInclusao;
    }

    public Participante getTentativaInclusao() {
        return this.tentativaInclusao;
    }

    public void setTentativaInclusao(Participante tentativaInclusao) {
        this.tentativaInclusao = tentativaInclusao;
    }
    
    public boolean isSucessoCadastro() {
        return this.sucessoCadastro;
    }

    public void setSucessoCadastro(boolean sucessoCadastro) {
        this.sucessoCadastro = sucessoCadastro;
    }

    public void setPalpiteAuthorizationService(PalpiteAuthorizationService palpiteAuthorizationService) {
        this.palpiteAuthorizationService = palpiteAuthorizationService;
    }

    private boolean validarCadastroEntradas() {
        boolean valido = true;
        List<MensagemErro> erros = new ArrayList<>();

        if (login == null || !SanitizationUtils.isValidLogin(login) || loginPossuiHtml) {
            registrarErro(erros, "signin.login", "cadastro.login.invalido");
            valido = false;
        }
        if (nome == null || nome.isBlank() || nome.length() < 3 || nomePossuiHtml) {
            registrarErro(erros, "signin.name", "cadastro.nome.invalido");
            valido = false;
        }
        if (email == null || !SanitizationUtils.isValidEmail(email) || emailPossuiHtml) {
            registrarErro(erros, "signin.email", "cadastro.email.invalido");
            valido = false;
        }
        if (!ValidacaoUtils.isSenhaValida(senha)) {
            registrarErro(erros, "signin.pwd", "cadastro.senha.invalida");
            valido = false;
        }

        this.errosInclusao = erros.isEmpty() ? null : erros;
        return valido;
    }

    private void registrarErro(List<MensagemErro> erros, String campoKey, String mensagemKey) {
        String campo = texto(campoKey, fallbackCampo(campoKey));
        String mensagem = texto(mensagemKey, fallbackMensagem(mensagemKey));
        erros.add(new MensagemErro(campo, mensagem, MensagemErro.SEVERIDADE_ERRO));
    }

    private boolean verificarDuplicidadeCadastro(Participante participante) {
        List<MensagemErro> duplicidades = new ArrayList<>();
        String loginNormalizado = participante.getLogin() == null ? null : participante.getLogin().trim().toLowerCase();
        if (loginNormalizado != null && !loginNormalizado.isBlank()) {
            if (getParticipanteService().buscarPorLogin(loginNormalizado).isPresent()) {
                duplicidades.add(new MensagemErro(
                        texto("signin.login", "Login"),
                        texto("cadastro.login.duplicado", "Ja existe um cadastro ativo com este login."),
                        MensagemErro.SEVERIDADE_ERRO));
            }
        }
        String emailNormalizado = participante.getEmail() == null ? null : participante.getEmail().trim();
        if (emailNormalizado != null && !emailNormalizado.isBlank()) {
            if (getParticipanteService().buscarPorEmail(emailNormalizado).isPresent()) {
                duplicidades.add(new MensagemErro(
                        texto("signin.email", "E-mail"),
                        texto("cadastro.email.duplicado", "Este e-mail ja esta associado a outro cadastro."),
                        MensagemErro.SEVERIDADE_ERRO));
            }
        }
        if (!duplicidades.isEmpty()) {
            if (this.errosInclusao == null) {
                this.errosInclusao = new ArrayList<>();
            }
            this.errosInclusao.addAll(duplicidades);
            return true;
        }
        return false;
    }

    private String texto(String key, String fallback) {
        try {
            String valor = getText(key);
            if (valor != null && !valor.isBlank() && !valor.equals(key)) {
                return valor;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String fallbackCampo(String key) {
        return switch (key) {
            case "signin.login" -> "Login";
            case "signin.name" -> "Nome";
            case "signin.email" -> "E-mail";
            case "signin.pwd" -> "Senha";
            default -> key;
        };
    }

    private String fallbackMensagem(String key) {
        return switch (key) {
            case "cadastro.login.invalido" -> "Login deve seguir o formato informado.";
            case "cadastro.nome.invalido" -> "Informe um nome completo válido, sem HTML.";
            case "cadastro.email.invalido" -> "Informe um e-mail válido no formato usuario@dominio.";
            case "cadastro.senha.invalida" -> "Informe uma senha entre 8 e 64 caracteres com símbolos seguros.";
            case "cadastro.login.duplicado" -> "Já existe um cadastro ativo com este login.";
            case "cadastro.email.duplicado" -> "Este e-mail já está associado a outro cadastro.";
            default -> key;
        };
    }

	public List getEquipes() {
		return equipes;
	}

	public void setEquipes(List equipes) {
		this.equipes = equipes;
	}

	public EquipeService getEquipeService() {
		return equipeService;
	}

	public void setEquipeService(EquipeService equipeService) {
		this.equipeService = equipeService;
	}

	public FiltroBuscaJogos getFiltro() {
		return filtro;
	}
	
	public void setFiltro(FiltroBuscaJogos filtro) {
		this.filtro = filtro;
	}

	   public long getProgressoPalpitesTotal() {
	       return progressoPalpitesTotal;
	   }

	   public void setProgressoPalpitesTotal(long progressoPalpitesTotal) {
	       this.progressoPalpitesTotal = progressoPalpitesTotal;
	   }

	   public long getProgressoPalpitesPreenchidos() {
	       return progressoPalpitesPreenchidos;
	   }

	   public void setProgressoPalpitesPreenchidos(long progressoPalpitesPreenchidos) {
	       this.progressoPalpitesPreenchidos = progressoPalpitesPreenchidos;
	   }

	   public int getProgressoPalpitesPercentual() {
	       return progressoPalpitesPercentual;
	   }

	   public void setProgressoPalpitesPercentual(int progressoPalpitesPercentual) {
	       this.progressoPalpitesPercentual = progressoPalpitesPercentual;
	   }

    
    public Participante getParticipanteLogado() {
        return this.participanteLogado;
    }
    
    public void setParticipanteLogado(Participante participanteLogado) {
        this.participanteLogado = participanteLogado;
    }
    
    public void setParticipantes(List participantes) {
        this.participantes = participantes;
    }
    
    public InputStream getGraficoStream() {
        return this.graficoStream;
    }

    public Long getJogoId() {
        return jogoId;
    }

    @StrutsParameter
    public void setJogoId(Long jogoId) {
        this.jogoId = jogoId;
    }

    @StrutsParameter
    public void setPalpiteGolsEquipe1(Integer palpiteGolsEquipe1) {
        this.palpiteGolsEquipe1 = palpiteGolsEquipe1;
    }

    @StrutsParameter
    public void setPalpiteGolsEquipe2(Integer palpiteGolsEquipe2) {
        this.palpiteGolsEquipe2 = palpiteGolsEquipe2;
    }

    public String getRival() {
        return rival;
    }

    @StrutsParameter
    public void setRival(String rival) {
        this.rival = rival;
    }

    public List getJogosDeHoje() {
        return this.jogosDeHoje;
    }

    public void setJogosDeHoje(List jogosDeHoje) {
        this.jogosDeHoje = jogosDeHoje;
    }

}
