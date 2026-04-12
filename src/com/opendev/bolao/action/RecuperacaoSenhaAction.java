package com.opendev.bolao.action;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.opendev.bolao.service.RecuperacaoSenhaService;
import com.opendev.bolao.service.dto.ResultadoTrocaSenha;
import com.opendev.bolao.service.dto.SolicitacaoOtp;
import com.opendev.bolao.service.dto.ValidacaoOtp;
import com.opendev.bolao.util.MensagemErro;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecuperacaoSenhaAction extends ActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacaoSenhaAction.class);

    private RecuperacaoSenhaService recuperacaoSenhaService;

    private String email;
    private String otp;
    private String novaSenha;
    private String confirmarSenha;

    private boolean emailPossuiHtml;
    private boolean otpPossuiHtml;

    private boolean otpEnviado;
    private boolean otpValido;
    private boolean senhaAtualizada;

    private String mensagemNeutra;
    private String mensagemResultado;

    private List<MensagemErro> errosRecuperacao;

    public String recuperarSenhaForm() {
        return SUCCESS;
    }

    public String enviarOtpRecuperacao() {
        String emailNormalizado = normalizarEmail(this.email);
        if (!SanitizationUtils.isValidEmail(emailNormalizado)
                || !ValidacaoUtils.isEmailValido(emailNormalizado)
                || emailPossuiHtml) {
            registrarErro("recuperacao.email.label", "recuperacao.email.invalido", "E-mail", "Informe um e-mail válido.");
            return INPUT;
        }

        String ip = RequestUtils.getIpDaRequisicao();
        HttpServletRequest request = RequestUtils.getRequest();
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        SolicitacaoOtp resultado = recuperacaoSenhaService.solicitarOtp(emailNormalizado, ip, userAgent);
        
        if (resultado != null && !resultado.isEnviado()) {
            registrarErro("recuperacao.servidor.erro", "recuperacao.email.falha", "Erro", resultado.getMensagemNeutra());
            return INPUT;
        }

        this.mensagemNeutra = resultado != null && resultado.getMensagemNeutra() != null
                ? resultado.getMensagemNeutra()
                : texto("recuperacao.mensagem.neutra", "Se o e-mail estiver cadastrado, enviaremos as instruções.");
        this.otpEnviado = true;
        this.email = emailNormalizado;
        LOGGER.info("[RECUPERACAO][ACTION] solicitacao OTP ip={} enviado=true", ip);
        return SUCCESS;
    }

    public String validarOtpRecuperacao() {
        String emailNormalizado = normalizarEmail(this.email);
        if (!SanitizationUtils.isValidEmail(emailNormalizado)
                || !ValidacaoUtils.isEmailValido(emailNormalizado)
                || emailPossuiHtml) {
            registrarErro("recuperacao.email.label", "recuperacao.email.invalido", "E-mail", "Informe um e-mail válido.");
            return INPUT;
        }
        if (otpPossuiHtml || otp == null || otp.isBlank()) {
            registrarErro("recuperacao.otp.label", "recuperacao.otp.invalido", "Código", "Código inválido ou expirado.");
            return INPUT;
        }

        String ip = RequestUtils.getIpDaRequisicao();
        ValidacaoOtp resultado = recuperacaoSenhaService.validarOtp(emailNormalizado, otp, ip);
        this.mensagemNeutra = resultado != null && resultado.getMensagemNeutra() != null
                ? resultado.getMensagemNeutra()
                : texto("recuperacao.mensagem.neutra", "Se o e-mail estiver cadastrado, enviaremos as instruções.");
        if (resultado != null && resultado.isValido()) {
            this.otpValido = true;
            this.email = emailNormalizado;
            this.otp = otp;
            LOGGER.info("[RECUPERACAO][ACTION] OTP validado ip={}", ip);
            return SUCCESS;
        }
        registrarErro("recuperacao.otp.label", "recuperacao.otp.invalido", "Código", "Código inválido ou expirado.");
        return INPUT;
    }

    public String redefinirSenha() {
        boolean valido = true;
        String emailNormalizado = normalizarEmail(this.email);

        if (!SanitizationUtils.isValidEmail(emailNormalizado)
                || !ValidacaoUtils.isEmailValido(emailNormalizado)
                || emailPossuiHtml) {
            registrarErro("recuperacao.email.label", "recuperacao.email.invalido", "E-mail", "Informe um e-mail válido.");
            valido = false;
        }
        if (otpPossuiHtml || otp == null || otp.isBlank()) {
            registrarErro("recuperacao.otp.label", "recuperacao.otp.invalido", "Código", "Código inválido ou expirado.");
            valido = false;
        }
        if (novaSenha == null || novaSenha.isBlank()) {
            registrarErro("recuperacao.senha.label", "recuperacao.senha.invalida", "Senha", "Senha inválida.");
            valido = false;
        }
        if (confirmarSenha == null || confirmarSenha.isBlank()) {
            registrarErro("recuperacao.senha.confirmar.label", "recuperacao.senha.confirma.invalida", "Confirmação", "As senhas não conferem.");
            valido = false;
        }
        if (valido && !novaSenha.equals(confirmarSenha)) {
            registrarErro("recuperacao.senha.confirmar.label", "recuperacao.senha.confirma.invalida", "Confirmação", "As senhas não conferem.");
            valido = false;
        }
        if (valido && !ValidacaoUtils.isSenhaValida(novaSenha)) {
            registrarErro("recuperacao.senha.label", "recuperacao.senha.invalida", "Senha", "Senha inválida.");
            valido = false;
        }
        if (!valido) {
            return INPUT;
        }

        String ip = RequestUtils.getIpDaRequisicao();
        ResultadoTrocaSenha resultado = recuperacaoSenhaService.redefinirSenha(emailNormalizado, otp, novaSenha, ip);
        if (resultado != null && resultado.isSucesso()) {
            this.senhaAtualizada = true;
            this.mensagemResultado = resultado.getMensagem();
            this.email = emailNormalizado;
            LOGGER.info("[RECUPERACAO][ACTION] senha atualizada ip={}", ip);
            return SUCCESS;
        }

        String mensagem = resultado != null && resultado.getMensagem() != null
                ? resultado.getMensagem()
                : texto("recuperacao.otp.invalido", "Código inválido ou expirado.");
        registrarErro("recuperacao.geral.label", "recuperacao.otp.invalido", "Recuperação de senha", mensagem);
        return INPUT;
    }

    private String normalizarEmail(String valor) {
        String sanitized = SanitizationUtils.cleanText(valor, 254);
        return sanitized == null ? null : sanitized.trim().toLowerCase();
    }

    private void registrarErro(String campoKey, String mensagemKey, String fallbackCampo, String fallbackMensagem) {
        if (this.errosRecuperacao == null) {
            this.errosRecuperacao = new ArrayList<>();
        }
        String campo = texto(campoKey, fallbackCampo);
        String mensagem = texto(mensagemKey, fallbackMensagem);
        this.errosRecuperacao.add(new MensagemErro(campo, mensagem, MensagemErro.SEVERIDADE_ERRO));
        
        HttpServletRequest request = RequestUtils.getRequest();
        if (request != null) {
            request.setAttribute("errosRecuperacao", this.errosRecuperacao);
        }
    }

    private String texto(String key, String fallback) {
        try {
            String valor = getText(key);
            if (valor != null && !valor.isBlank() && !valor.equals(key)) {
                return valor;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public void setRecuperacaoSenhaService(RecuperacaoSenhaService recuperacaoSenhaService) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    public List<MensagemErro> getErrosRecuperacao() {
        return errosRecuperacao;
    }

    public String getEmail() {
        return email;
    }

    @StrutsParameter
    public void setEmail(String email) {
        this.emailPossuiHtml = SanitizationUtils.containsHtml(email);
        this.email = SanitizationUtils.cleanText(email, 254);
    }

    public String getOtp() {
        return otp;
    }

    @StrutsParameter
    public void setOtp(String otp) {
        this.otpPossuiHtml = SanitizationUtils.containsHtml(otp);
        String sanitized = SanitizationUtils.cleanText(otp, 10);
        this.otp = sanitized == null ? null : sanitized.replaceAll("\\D", "");
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    @StrutsParameter
    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha == null ? null : novaSenha.trim();
    }

    public String getConfirmarSenha() {
        return confirmarSenha;
    }

    @StrutsParameter
    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha == null ? null : confirmarSenha.trim();
    }

    public boolean isOtpEnviado() {
        return otpEnviado;
    }

    public boolean isOtpValido() {
        return otpValido;
    }

    public boolean isSenhaAtualizada() {
        return senhaAtualizada;
    }

    public String getMensagemNeutra() {
        return mensagemNeutra;
    }

    public String getMensagemResultado() {
        return mensagemResultado;
    }
}
