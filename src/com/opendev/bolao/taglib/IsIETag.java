package com.opendev.bolao.taglib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class IsIETag extends TagSupport {

	private static final long serialVersionUID = 1L;

	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String ua = request.getHeader("user-agent");
		boolean isIE = ua.toLowerCase().indexOf("msie") != -1;
		if (isIE) {
			return EVAL_BODY_INCLUDE;	
		} else {
			return SKIP_BODY;
		}
	}

}
