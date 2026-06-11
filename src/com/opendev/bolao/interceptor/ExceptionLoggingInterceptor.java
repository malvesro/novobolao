package com.opendev.bolao.interceptor;

import com.opendev.bolao.service.ErrorNotificationService;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExceptionLoggingInterceptor extends AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggingInterceptor.class);

    @Autowired
    private ErrorNotificationService errorNotificationService;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        try {
            return invocation.invoke();
        } catch (Exception ex) {
            LOGGER.error("[EXCEPTION-INTERCEPTOR] Erro grave capturado: {}", ex.getMessage(), ex);
            
            // Notifica o administrador via e-mail assincronamente ou diretamente
            errorNotificationService.notificarErro(ex, "Erro grave na Action: " + invocation.getProxy().getActionName());
            
            // Relança a exceção para que o <global-exception-mappings> do struts.xml a capture
            throw ex;
        }
    }
}
