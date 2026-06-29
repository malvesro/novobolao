package com.opendev.bolao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.io.Serializable;
import java.util.Date;

/**
 * Mensagem persistida do Chat 2.0.
 */
@Entity
@Table(name = "CHT_CHAT_MENSAGEM", indexes = {
        @Index(name = "IDX_CHT_DATA_ENVIO", columnList = "CHT_DATA_ENVIO"),
        @Index(name = "IDX_CHT_LOGIN", columnList = "CHT_LOGIN_AUTOR"),
        @Index(name = "IDX_CHT_REPLY_TO", columnList = "CHT_REPLY_TO_ID")
})
public class ChatMensagem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHT_ID")
    private Long id;

    @Column(name = "CHT_LOGIN_AUTOR", nullable = false, length = 32)
    private String loginAutor;

    @Column(name = "CHT_NOME_EXIBICAO", nullable = false, length = 80)
    private String nomeExibicao;

    @Column(name = "CHT_TEXTO", nullable = false, length = 300)
    private String texto;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CHT_DATA_ENVIO", nullable = false)
    private Date dataEnvio;

    @Column(name = "CHT_REPLY_TO_ID")
    private Long replyToMensagemId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginAutor() {
        return loginAutor;
    }

    public void setLoginAutor(String loginAutor) {
        this.loginAutor = loginAutor;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public void setNomeExibicao(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Long getReplyToMensagemId() {
        return replyToMensagemId;
    }

    public void setReplyToMensagemId(Long replyToMensagemId) {
        this.replyToMensagemId = replyToMensagemId;
    }
}
