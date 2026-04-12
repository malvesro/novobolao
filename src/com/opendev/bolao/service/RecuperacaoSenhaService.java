package com.opendev.bolao.service;

import com.opendev.bolao.service.dto.ResultadoTrocaSenha;
import com.opendev.bolao.service.dto.SolicitacaoOtp;
import com.opendev.bolao.service.dto.ValidacaoOtp;

public interface RecuperacaoSenhaService {

    SolicitacaoOtp solicitarOtp(String email, String ip, String userAgent);

    ValidacaoOtp validarOtp(String email, String otp, String ip);

    ResultadoTrocaSenha redefinirSenha(String email, String otp, String novaSenha, String ip);
}
