package com.opendev.bolao.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Fábrica para obter o EmailSender configurado.
 * Implementa ApplicationContextAware para permitir acesso estático (ponte para legado).
 */
@Component
public class EmailSenderFactory implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderFactory.class);
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Retorna a implementação de EmailSender baseada na configuração.
     */
    public static EmailSender getSender() {
        if (context != null && context.containsBean("emailSender")) {
            return context.getBean("emailSender", EmailSender.class);
        }
        
        // Fallback básico para quando o Spring ainda não subiu (ex: testes simples)
        LOGGER.warn("Spring Context não disponível ou bean 'emailSender' não encontrado. Usando fallback SMTP.");
        return new SmtpEmailSender(EmailConfiguration.load());
    }
}
