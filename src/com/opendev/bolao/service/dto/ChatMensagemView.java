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

    public ChatMensagemView(Long id,
                            String loginAutor,
                            String nomeExibicao,
                            String texto,
                            Date dataEnvio,
                            boolean autoriaDoUsuarioAtual) {
        this.id = id;
        this.loginAutor = loginAutor;
        this.nomeExibicao = nomeExibicao;
        this.texto = texto;
        this.dataEnvio = dataEnvio;
        this.autoriaDoUsuarioAtual = autoriaDoUsuarioAtual;
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
}
