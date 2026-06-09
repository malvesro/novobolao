package com.opendev.bolao.action;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.OtpService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.MensagemErro;
import com.opendev.bolao.util.RequestUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidacaoCadastroAction extends ActionSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidacaoCadastroAction.class);
    private static final int MAX_TENTATIVAS = 3;

    private OtpService otpService;
    private ParticipanteService participanteService;

    private String codigo;
    private List<MensagemErro> errosValidacao;
    private int tentativasRestantes;
    private String novoEmail;

    public String exibir() {
        HttpSession session = RequestUtils.getRequest().getSession();
        Participante p = (Participante) session.getAttribute("PENDING_REGISTRATION");
        if (p == null) {
            return "redirectHome";
        }
        
        Integer tentativas = (Integer) session.getAttribute("REGISTRATION_OTP_TRIES");
        if (tentativas == null) {
            tentativas = 0;
            session.setAttribute("REGISTRATION_OTP_TRIES", tentativas);
        }
        this.tentativasRestantes = MAX_TENTATIVAS - tentativas;
        
        return SUCCESS;
    }

    public String validar() {
        HttpSession session = RequestUtils.getRequest().getSession();
        Participante p = (Participante) session.getAttribute("PENDING_REGISTRATION");
        if (p == null) {
            return "redirectHome";
        }

        Integer tentativas = (Integer) session.getAttribute("REGISTRATION_OTP_TRIES");
        if (tentativas == null) tentativas = 0;
        
        if (tentativas >= MAX_TENTATIVAS) {
            return "limiteExcedido";
        }

        if (otpService.validar(p.getEmail(), codigo)) {
            try {
                participanteService.criarNovo(p);
                session.removeAttribute("PENDING_REGISTRATION");
                session.removeAttribute("REGISTRATION_OTP_TRIES");
                otpService.consumir(p.getEmail());
                return "confirmado";
            } catch (Exception e) {
                LOGGER.error("[CADASTRO] Erro ao persistir participante apos validacao OTP", e);
                addActionError("Erro técnico ao finalizar o cadastro.");
                return ERROR;
            }
        } else {
            tentativas++;
            session.setAttribute("REGISTRATION_OTP_TRIES", tentativas);
            this.tentativasRestantes = MAX_TENTATIVAS - tentativas;
            
            List<MensagemErro> erros = new ArrayList<>();
            erros.add(new MensagemErro("Codigo", "Código inválido ou expirado.", MensagemErro.SEVERIDADE_ERRO));
            setErrosValidacao(erros);
            
            if (tentativas >= MAX_TENTATIVAS) {
                return "limiteExcedido";
            }
            return INPUT;
        }
    }

    public String reenviar() {
        HttpSession session = ServletActionContext.getRequest().getSession();
        Participante p = (Participante) session.getAttribute("PENDING_REGISTRATION");
        if (p == null) return "redirectHome";
        
        try {
            // Gera novo código
            String novoCodigo = otpService.gerarCodigo();
            otpService.armazenar(p.getEmail(), novoCodigo);
            
            // Envia e-mail
            Email emailEnvio = new Email("codigoValidacaoCadastro.html", "🜲 Nova Chave de Ativação: O Reforço do vosso Selo");
            emailEnvio.setPropriedade("nome", p.getNome());
            emailEnvio.setPropriedade("codigo", novoCodigo);
            emailEnvio.adicionarEnderecoDestino(p.getEmail());
            emailEnvio.enviar();
            
            // Reinicia tentativas
            session.setAttribute("REGISTRATION_OTP_TRIES", 0);
            
            LOGGER.info("[CADASTRO][OTP] Novo codigo reenviado para {}", p.getEmail());
            return "reenviado";
        } catch (Exception e) {
            LOGGER.error("[CADASTRO][OTP] Erro ao reenviar codigo OTP", e);
            addActionError("Erro ao reenviar o e-mail. Tente novamente.");
            return ERROR;
        }
    }

    // Getters e Setters
    public String getCodigo() { return codigo; }
    @StrutsParameter
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public List<MensagemErro> getErrosValidacao() { return errosValidacao; }
    public void setErrosValidacao(List<MensagemErro> errosValidacao) { this.errosValidacao = errosValidacao; }

    public int getTentativasRestantes() { return tentativasRestantes; }

    public void setOtpService(OtpService otpService) { this.otpService = otpService; }
    public void setParticipanteService(ParticipanteService participanteService) { this.participanteService = participanteService; }
}
