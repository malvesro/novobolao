package com.opendev.bolao.action;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.service.ChatService;
import com.opendev.bolao.service.dto.ChatMensagemView;
import com.opendev.bolao.service.dto.MentionNotification;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.action.ServletResponseAware;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAction.class);
    private static final int LIMITE_HISTORICO_MENCOES = 10;

    private ChatService chatService;
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;

    private List<ChatMensagemView> mensagensChat = Collections.emptyList();
    private List<String> participantesOnlineChat = Collections.emptyList();
    private List<MentionNotification> notificacoesMencao = Collections.emptyList();
    private List<MentionNotification> historicoMencoes = Collections.emptyList();
    private List<ChatMensagemView> mensagensConsulta = Collections.emptyList();
    private String chatErro;
    private Long chatUltimoId;
    private String chatMensagem;
    private Long chatReplyMensagemId;
    private String chatMencoesAckIds;
    private String chatBuscaTermo;
    private String chatBuscaAutor;
    private String chatBuscaDataInicio;
    private String chatBuscaDataFim;
    private int chatMencoesPendentes;
    private boolean chatMencoesColdStartAtivo;
    private boolean chatMencoesModoDegradado;

    public String exibirChat() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }

        atualizarSinalizacaoRuntimeMencoes();
        this.historicoMencoes = chatService.buscarHistoricoMencoes(login, LIMITE_HISTORICO_MENCOES);
        this.chatMencoesPendentes = chatService.contarMencoesPendentes(login);
        this.mensagensChat = chatService.buscarMensagensIniciais(login);
        this.participantesOnlineChat = chatService.buscarParticipantesOnline();
        this.chatUltimoId = obterMaiorId(this.mensagensChat, this.chatUltimoId);
        return SUCCESS;
    }

    public String carregarMensagensParcial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }

        try {
            this.mensagensChat = chatService.buscarMensagensIncrementais(login, this.chatUltimoId);
            this.participantesOnlineChat = chatService.buscarParticipantesOnline();
            this.chatUltimoId = obterMaiorId(this.mensagensChat, this.chatUltimoId);
            return SUCCESS;
        } catch (BusinessException e) {
            int status = mapearStatusBusiness(e);
            setStatus(status);
            this.chatErro = e.getMessage();
            LOGGER.warn("[CHAT][POLL] bloqueado user={} status={} code={}", login, status, e.getCode());
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][POLL] erro ao carregar mensagens", e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.load", "Não foi possível atualizar as mensagens agora.");
            return SUCCESS;
        }
    }

    public String verificarMencoesPartial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }

        try {
            atualizarSinalizacaoRuntimeMencoes();
            this.notificacoesMencao = chatService.buscarMencoesPendentes(login);
            if (this.notificacoesMencao == null || this.notificacoesMencao.isEmpty()) {
                setStatus(HttpServletResponse.SC_NO_CONTENT);
                return NONE;
            }
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][MENTION] erro ao buscar notificacoes", e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.load", "Não foi possível atualizar as notificações agora.");
            return SUCCESS;
        }
    }

    public String verificarMencoesBadgePartial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }

        try {
            atualizarSinalizacaoRuntimeMencoes();
            this.chatMencoesPendentes = chatService.contarMencoesPendentes(login);
            if (this.chatMencoesPendentes <= 0) {
                setStatus(HttpServletResponse.SC_NO_CONTENT);
                return NONE;
            }
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][MENTION_BADGE] erro ao buscar contagem de mencoes", e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.load", "Não foi possível atualizar as notificações agora.");
            return SUCCESS;
        }
    }

    public String enviarMensagemParcial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }
        if (!isPost()) {
            setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            this.chatErro = texto("chat.error.method", "Método HTTP inválido para envio.");
            return SUCCESS;
        }

        try {
            ChatMensagemView mensagem = chatService.criarMensagem(
                    login, obterChaveSessaoChat(login), this.chatMensagem, RequestUtils.getIpDaRequisicao(), this.chatReplyMensagemId);
            this.mensagensChat = Collections.singletonList(mensagem);
            this.participantesOnlineChat = chatService.buscarParticipantesOnline();
            this.chatUltimoId = mensagem.getId();
            return SUCCESS;
        } catch (BusinessException e) {
            int status = mapearStatusBusiness(e);
            setStatus(status);
            this.chatErro = e.getMessage();
            LOGGER.warn("[CHAT][SEND] bloqueado user={} status={} code={}",
                    login, status, e.getCode());
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][SEND] erro inesperado user={}", login, e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.send", "Falha ao enviar mensagem. Tente novamente.");
            return SUCCESS;
        }
    }

    public String confirmarMencoesParcial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }
        if (!isPost()) {
            setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            this.chatErro = texto("chat.error.method", "Método HTTP inválido para envio.");
            return SUCCESS;
        }
        if (!isAjaxRequest()) {
            setStatus(HttpServletResponse.SC_FORBIDDEN);
            this.chatErro = texto("chat.error.csrf", "Requisição inválida para confirmação de menções.");
            return SUCCESS;
        }

        try {
            atualizarSinalizacaoRuntimeMencoes();
            chatService.confirmarMencoesPendentes(login, parseChatMencoesAckIds());
            this.chatMencoesPendentes = chatService.contarMencoesPendentes(login);
            setStatus(HttpServletResponse.SC_NO_CONTENT);
            return NONE;
        } catch (Exception e) {
            LOGGER.error("[CHAT][MENTION_ACK] erro ao confirmar mencoes", e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.load", "Não foi possível atualizar as notificações agora.");
            return SUCCESS;
        }
    }

    public String consultarHistoricoParcial() {
        marcarRespostaParcial();
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }
        if (!isAjaxRequest()) {
            setStatus(HttpServletResponse.SC_FORBIDDEN);
            this.chatErro = texto("chat.error.csrf", "Requisição inválida para consulta de histórico.");
            return SUCCESS;
        }

        try {
            Date dataInicio = parseDataFiltro(this.chatBuscaDataInicio, true);
            Date dataFim = parseDataFiltro(this.chatBuscaDataFim, false);
            this.mensagensConsulta = chatService.buscarHistoricoFiltrado(
                    login,
                    this.chatBuscaTermo,
                    this.chatBuscaAutor,
                    dataInicio,
                    dataFim,
                    80
            );
            if (this.mensagensConsulta == null || this.mensagensConsulta.isEmpty()) {
                setStatus(HttpServletResponse.SC_NO_CONTENT);
                return NONE;
            }
            return SUCCESS;
        } catch (BusinessException e) {
            setStatus(mapearStatusBusiness(e));
            this.chatErro = e.getMessage();
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][QUERY] erro ao consultar histórico", e);
            setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.chatErro = texto("chat.error.query", "Não foi possível consultar o histórico agora.");
            return SUCCESS;
        }
    }

    private int mapearStatusBusiness(BusinessException e) {
        if (e.getCode() == BusinessException.Code.CONFLICT) {
            if (response != null) {
                response.setHeader("Retry-After", "10");
            }
            return 429;
        }
        return HttpServletResponse.SC_BAD_REQUEST;
    }

    private Long obterMaiorId(List<ChatMensagemView> mensagens, Long fallback) {
        Long maior = fallback == null ? 0L : fallback;
        if (mensagens == null) {
            return maior;
        }
        for (ChatMensagemView mensagem : mensagens) {
            if (mensagem != null && mensagem.getId() != null && mensagem.getId() > maior) {
                maior = mensagem.getId();
            }
        }
        return maior;
    }

    private void marcarRespostaParcial() {
        if (request != null) {
            request.setAttribute("skipTemplate", Boolean.TRUE);
        }
    }

    private boolean isPost() {
        return request != null && "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean isAjaxRequest() {
        if (request == null) {
            return false;
        }
        String requestedWith = request.getHeader("X-Requested-With");
        String hxRequest = request.getHeader("HX-Request");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || "true".equalsIgnoreCase(hxRequest);
    }

    private void setStatus(int status) {
        if (response != null) {
            response.setStatus(status);
        }
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

    private String obterChaveSessaoChat(String login) {
        if (request != null) {
            HttpSession sessao = request.getSession(true);
            if (sessao != null && !ValidacaoUtils.isVazia(sessao.getId())) {
                return sessao.getId().trim();
            }
        }
        return login == null ? "anonimo" : login.trim().toLowerCase(Locale.ROOT);
    }

    private List<Long> parseChatMencoesAckIds() {
        if (ValidacaoUtils.isVazia(this.chatMencoesAckIds)) {
            return Collections.emptyList();
        }
        String[] partes = this.chatMencoesAckIds.split(",");
        List<Long> ids = new ArrayList<>(partes.length);
        for (String parte : partes) {
            if (ValidacaoUtils.isVazia(parte)) {
                continue;
            }
            try {
                long valor = Long.parseLong(parte.trim());
                if (valor > 0) {
                    ids.add(valor);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private Date parseDataFiltro(String valor, boolean inicio) {
        if (ValidacaoUtils.isVazia(valor)) {
            return null;
        }
        LocalDate data = LocalDate.parse(valor.trim());
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        if (inicio) {
            return Date.from(data.atStartOfDay(zoneId).toInstant());
        }
        return Date.from(data.atTime(LocalTime.MAX).atZone(zoneId).toInstant());
    }

    private void atualizarSinalizacaoRuntimeMencoes() {
        this.chatMencoesColdStartAtivo = chatService.isMencoesColdStartAtivo();
        this.chatMencoesModoDegradado = chatService.isMencoesModoDegradado();
        if (response != null) {
            response.setHeader("X-Chat-Mentions-Cold-Start", Boolean.toString(this.chatMencoesColdStartAtivo));
            response.setHeader("X-Chat-Mentions-Degraded", Boolean.toString(this.chatMencoesModoDegradado));
            response.setHeader("X-Chat-Mentions-Delivery-Mode",
                    this.chatMencoesModoDegradado ? "memory-local-ephemeral" : "standard");
        }
    }

    @Override
    public void withServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void withServletResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public List<ChatMensagemView> getMensagensChat() {
        return mensagensChat;
    }

    public List<String> getParticipantesOnlineChat() {
        return participantesOnlineChat;
    }

    public List<MentionNotification> getNotificacoesMencao() {
        return notificacoesMencao;
    }

    public List<MentionNotification> getHistoricoMencoes() {
        return historicoMencoes;
    }

    public String getChatErro() {
        return chatErro;
    }

    public List<ChatMensagemView> getMensagensConsulta() {
        return mensagensConsulta;
    }

    public Long getChatUltimoId() {
        return chatUltimoId;
    }

    public int getChatMencoesPendentes() {
        return chatMencoesPendentes;
    }

    public boolean isChatMencoesColdStartAtivo() {
        return chatMencoesColdStartAtivo;
    }

    public boolean isChatMencoesModoDegradado() {
        return chatMencoesModoDegradado;
    }

    @StrutsParameter
    public void setChatUltimoId(Long chatUltimoId) {
        this.chatUltimoId = chatUltimoId;
    }

    @StrutsParameter
    public void setChatMensagem(String chatMensagem) {
        this.chatMensagem = chatMensagem;
    }

    @StrutsParameter
    public void setChatReplyMensagemId(Long chatReplyMensagemId) {
        this.chatReplyMensagemId = chatReplyMensagemId;
    }

    @StrutsParameter
    public void setChatMencoesAckIds(String chatMencoesAckIds) {
        this.chatMencoesAckIds = chatMencoesAckIds;
    }

    @StrutsParameter
    public void setChatBuscaTermo(String chatBuscaTermo) {
        this.chatBuscaTermo = chatBuscaTermo;
    }

    @StrutsParameter
    public void setChatBuscaAutor(String chatBuscaAutor) {
        this.chatBuscaAutor = chatBuscaAutor;
    }

    @StrutsParameter
    public void setChatBuscaDataInicio(String chatBuscaDataInicio) {
        this.chatBuscaDataInicio = chatBuscaDataInicio;
    }

    @StrutsParameter
    public void setChatBuscaDataFim(String chatBuscaDataFim) {
        this.chatBuscaDataFim = chatBuscaDataFim;
    }
}
