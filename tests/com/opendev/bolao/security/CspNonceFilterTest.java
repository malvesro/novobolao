package com.opendev.bolao.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CspNonceFilterTest {

    @Test
    @DisplayName("deve gerar nonce por request e publicar header CSP")
    void deveGerarNonceEHeaderCsp() throws Exception {
        CspNonceFilter filter = new CspNonceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/seguro/graficoDesempenho.action");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Object nonceAttr = request.getAttribute(CspNonceFilter.CSP_NONCE_ATTRIBUTE);
        assertThat(nonceAttr).isInstanceOf(String.class);
        assertThat((String) nonceAttr).isNotBlank();

        String csp = response.getHeader("Content-Security-Policy");
        assertThat(csp).isNotBlank();
        assertThat(csp).contains("script-src 'self' 'nonce-");

        String cspReportOnly = response.getHeader("Content-Security-Policy-Report-Only");
        assertThat(cspReportOnly).isEqualTo(csp);
    }

    @Test
    @DisplayName("deve gerar nonces distintos entre requests consecutivos")
    void deveGerarNoncesDistintosEntreRequests() throws Exception {
        CspNonceFilter filter = new CspNonceFilter();

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/seguro/jogos.action");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request1, response1, new MockFilterChain());

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/seguro/jogos.action");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request2, response2, new MockFilterChain());

        String nonce1 = (String) request1.getAttribute(CspNonceFilter.CSP_NONCE_ATTRIBUTE);
        String nonce2 = (String) request2.getAttribute(CspNonceFilter.CSP_NONCE_ATTRIBUTE);

        assertThat(nonce1).isNotBlank();
        assertThat(nonce2).isNotBlank();
        assertThat(nonce1).isNotEqualTo(nonce2);
        assertThat(response1.getHeader("Content-Security-Policy")).contains("'nonce-" + nonce1 + "'");
        assertThat(response2.getHeader("Content-Security-Policy")).contains("'nonce-" + nonce2 + "'");
    }
}
