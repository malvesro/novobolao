package com.opendev.bolao.service.dto;

public class ValidacaoOtp {

    private final boolean valido;
    private final String mensagemNeutra;

    public ValidacaoOtp(boolean valido, String mensagemNeutra) {
        this.valido = valido;
        this.mensagemNeutra = mensagemNeutra;
    }

    public boolean isValido() {
        return valido;
    }

    public String getMensagemNeutra() {
        return mensagemNeutra;
    }
}
