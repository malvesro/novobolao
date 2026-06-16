package com.opendev.bolao.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Generates a CSP nonce per request and applies the Content-Security-Policy header
 * so that inline scripts can opt-in via {@code nonce="${cspNonce}"}.
 */
public class CspNonceFilter extends OncePerRequestFilter {

    public static final String CSP_NONCE_ATTRIBUTE = "cspNonce";
    private static final Logger LOGGER = LoggerFactory.getLogger(CspNonceFilter.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String nonce = generateNonce();
        request.setAttribute(CSP_NONCE_ATTRIBUTE, nonce);

        String policy = buildPolicy(nonce);
        response.setHeader("Content-Security-Policy", policy);
        response.setHeader("Content-Security-Policy-Report-Only", policy);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[SEC][CSP] nonce gerado para uri={}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    private static String buildPolicy(String nonce) {
        return "default-src 'self' https://novobolaodacopa-bolaocopa.hf.space; "
            + "script-src 'self' 'nonce-" + nonce + "' 'strict-dynamic' https://novobolaodacopa-bolaocopa.hf.space; "
            + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://novobolaodacopa-bolaocopa.hf.space; "
            + "font-src 'self' data: https://fonts.gstatic.com https://novobolaodacopa-bolaocopa.hf.space; "
            + "img-src 'self' data: https://novobolaodacopa-bolaocopa.hf.space; "
            + "connect-src 'self' https://novobolaodacopa-bolaocopa.hf.space; "
            + "form-action 'self' https://novobolaodacopa-bolaocopa.hf.space; "
            + "frame-ancestors 'self' https://novobolaodacopa-bolaocopa.hf.space; "
            + "base-uri 'self'";
    }
}
