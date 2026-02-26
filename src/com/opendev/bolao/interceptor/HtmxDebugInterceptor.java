package com.opendev.bolao.interceptor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.struts2.ActionInvocation;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.interceptor.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

public class HtmxDebugInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmxDebugInterceptor.class);

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		HttpServletRequest request = (HttpServletRequest) invocation
				.getInvocationContext()
				.get(StrutsStatics.HTTP_REQUEST);
		if (request != null && hasHtmxHeaders(request)) {
			Map<String, String> headers = captureHeaders(request);
			LOGGER.info("[HTMX-TRACE][interceptor][action={}] headers={}",
					invocation.getProxy().getActionName(),
					headers);
		}
		return invocation.invoke();
	}

	private boolean hasHtmxHeaders(HttpServletRequest request) {
		return request.getHeader("HX-Request") != null
				|| request.getHeader("X-Requested-With") != null;
	}

	private Map<String, String> captureHeaders(HttpServletRequest request) {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("HX-Request", sanitize(request.getHeader("HX-Request")));
		headers.put("X-Requested-With", sanitize(request.getHeader("X-Requested-With")));
		headers.put("Sec-Fetch-Site", sanitize(request.getHeader("Sec-Fetch-Site")));
		headers.put("Sec-Fetch-Mode", sanitize(request.getHeader("Sec-Fetch-Mode")));
		headers.put("Sec-Fetch-Dest", sanitize(request.getHeader("Sec-Fetch-Dest")));
		headers.put("Sec-Fetch-User", sanitize(request.getHeader("Sec-Fetch-User")));
		headers.put("Origin", sanitize(request.getHeader("Origin")));
		headers.put("Referer", sanitize(request.getHeader("Referer")));
		return headers;
	}

	private String sanitize(String value) {
		if (value == null) {
			return null;
		}
		return value.replaceAll("[\\r\\n]", "");
	}
}
