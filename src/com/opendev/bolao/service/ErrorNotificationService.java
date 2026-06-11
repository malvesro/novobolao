package com.opendev.bolao.service;

import com.opendev.bolao.email.Email;
import com.opendev.bolao.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ErrorNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorNotificationService.class);

    @Autowired
    private MessageSource messageSource;

    public void notificarErro(Throwable ex, String descricao) {
        try {
            String adminEmail = messageSource.getMessage("admin.email.notificacao", null, "novobolaocopa@gmail.com", Locale.getDefault());
            HttpServletRequest request = RequestUtils.getRequest();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String usuario = RequestUtils.getLoginParticipanteAutenticado();
            String url = (request != null) ? request.getRequestURL().toString() : "N/A";
            String stackTrace = getStackTraceAsString(ex);

            Email email = new Email("notificacaoErro.html", "⚠️ Alerta de Erro Crítico - Bolão 2026");
            email.adicionarEnderecoDestino(adminEmail);
            email.setPropriedade("timestamp", timestamp);
            email.setPropriedade("usuario", usuario != null ? usuario : "Anônimo");
            email.setPropriedade("url", url);
            email.setPropriedade("descricao", descricao);
            email.setPropriedade("excecao", ex.toString());
            email.setPropriedade("stackTrace", stackTrace);
            
            email.enviar();
            LOGGER.info("[ERROR-NOTIF] Notificação de erro enviada para {}", adminEmail);
        } catch (Exception e) {
            LOGGER.error("[ERROR-NOTIF] Falha ao enviar notificação de erro", e);
        }
    }

    private String getStackTraceAsString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
