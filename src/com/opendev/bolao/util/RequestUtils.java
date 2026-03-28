package com.opendev.bolao.util;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);

    private RequestUtils() {
        // utilitário apenas com métodos estáticos
    }

    public static HttpServletRequest getRequest() {
        HttpServletRequest request = null;
        ActionContext actionContext = ActionContext.getContext();
        if (actionContext != null) {
            Object requestAttribute = actionContext.get(ServletActionContext.HTTP_REQUEST);
            if (requestAttribute instanceof HttpServletRequest strutsRequest) {
                return strutsRequest;
            }
        }
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttributes) {
            request = servletAttributes.getRequest();
        }
        return request;
    }

    public static String getLoginParticipanteAutenticado() {
        HttpServletRequest request = getRequest();
        Principal principal = request != null ? request.getUserPrincipal() : null;
        if (principal != null) {
            if (LOGGER.isInfoEnabled() && request != null) {
                boolean htmx = "true".equalsIgnoreCase(request.getHeader("HX-Request"));
                if (htmx) {
                    LOGGER.info("[SEC][HTMX] principal recuperado via HttpServletRequest name={}", principal.getName());
                }
            }
            return principal.getName();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        boolean autenticado = authentication.isAuthenticated();
        Object authPrincipal = authentication.getPrincipal();
        boolean principalAnonimo = authPrincipal == null
                || "anonymousUser".equalsIgnoreCase(String.valueOf(authPrincipal));

        if (autenticado && !principalAnonimo) {
            String fallback = authentication.getName();
            if (LOGGER.isInfoEnabled()) {
                boolean htmx = request != null && "true".equalsIgnoreCase(request.getHeader("HX-Request"));
                LOGGER.info(
                        "[SEC][HTMX] principal recuperado via SecurityContext name={} principalClass={} authorities={} htmxRequest={}",
                        fallback,
                        authPrincipal.getClass().getName(),
                        authentication.getAuthorities(),
                        htmx);
            }
            return fallback;
        }

        if (LOGGER.isDebugEnabled()) {
            boolean htmx = request != null && "true".equalsIgnoreCase(request.getHeader("HX-Request"));
            LOGGER.debug(
                    "[SEC][HTMX] principal ausente (autenticado={} principalAnonimo={}, authenticationClass={}, htmxRequest={})",
                    autenticado,
                    principalAnonimo,
                    authentication.getClass().getName(),
                    htmx);
        }

        return null;
    }

    public static String getIpDaRequisicao() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRemoteAddr() : null;
    }

//    public static FiltroBuscaJogos getFiltroBuscaJogos() {
//        HttpSession session = getRequest().getSession();
//        return (FiltroBuscaJogos) session.getAttribute(FiltroBuscaJogos.FILTRO_JOGOS_SESSION);
//    }

}
