package com.opendev.bolao.util;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.security.core.session.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContadorParticipantesOnline implements HttpSessionListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ContadorParticipantesOnline.class);

    public void sessionCreated(HttpSessionEvent ev) {
        // O SessionRegistry cuidará do registro no momento da autenticação
    }

    public void sessionDestroyed(HttpSessionEvent ev) {
        // O SessionRegistry cuidará da expiração via HttpSessionEventPublisher
    }
    
    /**
     * Retorna o número de usuários autenticados no sistema.
     */
    public static int getNumeroDeParticipantesOnline() {
        try {
            SessionRegistry registry = SpringContextHolder.getBean(SessionRegistry.class);
            if (registry != null) {
                // Retorna apenas usuários autenticados (principals únicos)
                return registry.getAllPrincipals().size();
            }
        } catch (Exception e) {
            LOGGER.warn("[SISTEMA] Falha ao recuperar SessionRegistry para contagem de usuários online: {}", e.getMessage());
        }
        return 0;
    }

}
