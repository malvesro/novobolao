package com.opendev.bolao.service.dto;

public class SolicitacaoOtp {

    private final boolean enviado;
    private final String mensagemNeutra;

    public SolicitacaoOtp(boolean enviado, String mensagemNeutra) {
        this.enviado = enviado;
        this.mensagemNeutra = mensagemNeutra;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public String getMensagemNeutra() {
        return mensagemNeutra;
    }
}
