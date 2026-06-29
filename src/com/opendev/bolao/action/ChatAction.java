package com.opendev.bolao.action;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.service.ChatNotificationService;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChatAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAction.class);

    private ChatService chatService;
    private ChatNotificationService chatNotificationService;
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;

    private List<ChatMensagemView> mensagensChat = Collections.emptyList();
    private List<String> participantesOnlineChat = Collections.emptyList();
    private List<MentionNotification> notificacoesMencao = Collections.emptyList();
    private String chatErro;
    private Long chatUltimoId;
    private String chatMensagem;

    public String exibirChat() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (ValidacaoUtils.isVazia(login)) {
            return LOGIN;
        }

        if (chatNotificationService != null) {
            chatNotificationService.buscarMencoesPendentes(login);
        }
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
            this.notificacoesMencao = chatNotificationService.buscarMencoesPendentes(login);
            if (this.notificacoesMencao == null || this.notificacoesMencao.isEmpty()) {
                setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error("[CHAT][MENTION] erro ao buscar notificacoes", e);
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
                    login, obterChaveSessaoChat(login), this.chatMensagem, RequestUtils.getIpDaRequisicao());
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

    public void setChatNotificationService(ChatNotificationService chatNotificationService) {
        this.chatNotificationService = chatNotificationService;
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

    public String getChatErro() {
        return chatErro;
    }

    public Long getChatUltimoId() {
        return chatUltimoId;
    }

    @StrutsParameter
    public void setChatUltimoId(Long chatUltimoId) {
        this.chatUltimoId = chatUltimoId;
    }

    @StrutsParameter
    public void setChatMensagem(String chatMensagem) {
        this.chatMensagem = chatMensagem;
    }
}
