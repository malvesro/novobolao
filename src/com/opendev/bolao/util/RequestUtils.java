package com.opendev.bolao.util;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;

import com.opensymphony.webwork.ServletActionContext;

public final class RequestUtils {
	
	public static final HttpServletRequest getRequest() {
		HttpServletRequest request = ServletActionContext.getRequest();
		if (request == null) {
			WebContext webCtx = WebContextFactory.get();
			request = webCtx.getHttpServletRequest();
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
