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
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.util.Date;

/**
 * Menção persistida para entrega cross-screen e histórico.
 */
@Entity
@Table(name = "CHT_CHAT_MENCAO", indexes = {
        @Index(name = "IDX_CHT_MENCAO_DEST_PEND", columnList = "CHM_DEST_LOGIN,CHM_DATA_CONFIRMACAO,CHM_DATA_CRIACAO"),
        @Index(name = "IDX_CHT_MENCAO_DEST_DATA", columnList = "CHM_DEST_LOGIN,CHM_DATA_CRIACAO")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_CHT_MENCAO_DEST_MSG", columnNames = {"CHM_DEST_LOGIN", "CHM_CHT_ID"})
})
public class ChatMencao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHM_ID")
    private Long id;

    @Column(name = "CHM_DEST_LOGIN", nullable = false, length = 32)
    private String destinatarioLogin;

    @Column(name = "CHM_AUTOR_LOGIN", nullable = false, length = 32)
    private String autorLogin;

    @Column(name = "CHM_AUTOR_NOME", nullable = false, length = 80)
    private String autorNomeExibicao;

    @Column(name = "CHM_CHT_ID", nullable = false)
    private Long chatMensagemId;

    @Column(name = "CHM_PREVIEW", nullable = false, length = 120)
    private String mensagemPreview;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CHM_DATA_CRIACAO", nullable = false)
    private Date dataCriacao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CHM_DATA_CONFIRMACAO")
    private Date dataConfirmacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDestinatarioLogin() {
        return destinatarioLogin;
    }

    public void setDestinatarioLogin(String destinatarioLogin) {
        this.destinatarioLogin = destinatarioLogin;
    }

    public String getAutorLogin() {
        return autorLogin;
    }

    public void setAutorLogin(String autorLogin) {
        this.autorLogin = autorLogin;
    }

    public String getAutorNomeExibicao() {
        return autorNomeExibicao;
    }

    public void setAutorNomeExibicao(String autorNomeExibicao) {
        this.autorNomeExibicao = autorNomeExibicao;
    }

    public Long getChatMensagemId() {
        return chatMensagemId;
    }

    public void setChatMensagemId(Long chatMensagemId) {
        this.chatMensagemId = chatMensagemId;
    }

    public String getMensagemPreview() {
        return mensagemPreview;
    }

    public void setMensagemPreview(String mensagemPreview) {
        this.mensagemPreview = mensagemPreview;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(Date dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }
}
