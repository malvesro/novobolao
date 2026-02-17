package com.opendev.bolao.util;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestUtils {
	
	public static final HttpServletRequest getRequest() {
		HttpServletRequest request = null;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) attrs).getRequest();
        }
		return request;
	}
	
	public static String getLoginParticipanteAutenticado() {
		Principal principal = getRequest().getUserPrincipal();
		String login = null;
		if (principal != null) {
			login = principal.getName();
		}
		return login;
	}
	
	public static String getIpDaRequisicao() {
		return getRequest().getRemoteAddr();
	}
	
//	public static FiltroBuscaJogos getFiltroBuscaJogos() {
//		HttpSession session = getRequest().getSession();
//		return (FiltroBuscaJogos) session.getAttribute(FiltroBuscaJogos.FILTRO_JOGOS_SESSION);
//	}

}
