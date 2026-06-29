package com.opendev.bolao.service.dto;

import java.util.Date;

public class MentionNotification {

    private final String autorLogin;
    private final String autorNomeExibicao;
    private final Long chatMensagemId;
    private final String mensagemPreview;
    private final Date dataCriacao;

    public MentionNotification(String autorLogin,
                               String autorNomeExibicao,
                               Long chatMensagemId,
                               String mensagemPreview) {
        this.autorLogin = autorLogin;
        this.autorNomeExibicao = autorNomeExibicao;
        this.chatMensagemId = chatMensagemId;
        this.mensagemPreview = mensagemPreview;
        this.dataCriacao = new Date();
    }

    public String getAutorLogin() {
        return autorLogin;
    }

    public String getAutorNomeExibicao() {
        return autorNomeExibicao;
    }

    public Long getChatMensagemId() {
        return chatMensagemId;
    }

    public String getMensagemPreview() {
        return mensagemPreview;
    }

    public Date getDataCriacao() {
        return new Date(dataCriacao.getTime());
    }
}
