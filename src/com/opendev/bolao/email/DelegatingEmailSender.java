package com.opendev.bolao.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegador que escolhe o provedor de e-mail baseado na configuração.
 */
public class DelegatingEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingEmailSender.class);
    private final EmailConfiguration configuration;

    public DelegatingEmailSender(EmailConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void enviar(EmailMessage message) throws Exception {
        EmailSender sender = getActualSender();
        sender.enviar(message);
    }

    private EmailSender getActualSender() {
        String provider = configuration.getProperty("mail.provider");
        
        if ("brevo".equalsIgnoreCase(provider)) {
            String apiKey = configuration.getProperty("mail.brevo.api.key");
            LOGGER.debug("[EMAIL] Usando provedor Brevo REST API.");
            return new BrevoEmailSender(apiKey);
        }
        
        LOGGER.debug("[EMAIL] Usando provedor SMTP padrão.");
        return new SmtpEmailSender(configuration);
    }
}
