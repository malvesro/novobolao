package com.opendev.bolao.service.dto;

import java.util.Date;

/**
 * DTO de exibição do chat para evitar acoplamento da view com a entidade.
 */
public class ChatMensagemView {

    private final Long id;
    private final String loginAutor;
    private final String nomeExibicao;
    private final String texto;
    private final Date dataEnvio;
    private final boolean autoriaDoUsuarioAtual;
    private final Long replyToMensagemId;
    private final String replyToNomeExibicao;
    private final String replyToTextoPreview;
    private final Date replyToDataEnvio;

    public ChatMensagemView(Long id,
                            String loginAutor,
                            String nomeExibicao,
                            String texto,
                            Date dataEnvio,
                            boolean autoriaDoUsuarioAtual) {
        this(id, loginAutor, nomeExibicao, texto, dataEnvio, autoriaDoUsuarioAtual, null, null, null, null);
    }

    public ChatMensagemView(Long id,
                            String loginAutor,
                            String nomeExibicao,
                            String texto,
                            Date dataEnvio,
                            boolean autoriaDoUsuarioAtual,
                            Long replyToMensagemId,
                            String replyToNomeExibicao,
                            String replyToTextoPreview,
                            Date replyToDataEnvio) {
        this.id = id;
        this.loginAutor = loginAutor;
        this.nomeExibicao = nomeExibicao;
        this.texto = texto;
        this.dataEnvio = dataEnvio;
        this.autoriaDoUsuarioAtual = autoriaDoUsuarioAtual;
        this.replyToMensagemId = replyToMensagemId;
        this.replyToNomeExibicao = replyToNomeExibicao;
        this.replyToTextoPreview = replyToTextoPreview;
        this.replyToDataEnvio = replyToDataEnvio;
    }

    public Long getId() {
        return id;
    }

    public String getLoginAutor() {
        return loginAutor;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getTexto() {
        return texto;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public boolean isAutoriaDoUsuarioAtual() {
        return autoriaDoUsuarioAtual;
    }

    public Long getReplyToMensagemId() {
        return replyToMensagemId;
    }

    public String getReplyToNomeExibicao() {
        return replyToNomeExibicao;
    }

    public String getReplyToTextoPreview() {
        return replyToTextoPreview;
    }

    public Date getReplyToDataEnvio() {
        return replyToDataEnvio;
    }
}
