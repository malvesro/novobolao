package com.opendev.bolao.util;

import java.io.Serializable;

public class MensagemErro implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final int SEVERIDADE_ERRO = 1;
    public static final int SEVERIDADE_AVISO = 2;
    

    private String nomeDoCampo;
    private String mensagem;
    private int severidade;
    

    public MensagemErro(String fieldName, String message, int severity) {
        this.nomeDoCampo = fieldName;
        this.mensagem = message;
        this.severidade = severity;
    }
    
    public MensagemErro(String fieldName, String message) {
        this(fieldName, message, SEVERIDADE_AVISO);
    }
    
    public MensagemErro(String message) {
        this(null, message, SEVERIDADE_AVISO);
    }
    
    public MensagemErro(String message, int severity) {
        this(null, message, severity);
    }
    

    public String getNomeDoCampo() {
        return nomeDoCampo;
    }

    public void setNomeDoCampo(String fieldName) {
        this.nomeDoCampo = fieldName;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String message) {
        this.mensagem = message;
    }

    public int getSeveridade() {
        return severidade;
    }

    public void setSeveridade(int severity) {
        this.severidade = severity;
    }

}