package com.opendev.bolao.service.dto;

public class ResultadoTrocaSenha {

    private final boolean sucesso;
    private final String mensagem;

    public ResultadoTrocaSenha(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }
}
