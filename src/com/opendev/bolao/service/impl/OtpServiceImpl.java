package com.opendev.bolao.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opendev.bolao.service.OtpService;

public class OtpServiceImpl implements OtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!?";
    private static final int CODE_LENGTH = 6;
    private static final Duration EXPIRATION = Duration.ofMinutes(15);

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpData> store = new ConcurrentHashMap<>();

    @Override
    public String gerarCodigo() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean validar(String identificador, String codigo) {
        OtpData data = store.get(identificador);
        if (data == null || data.isExpired()) {
            store.remove(identificador);
            return false;
        }
        
        if (data.codigo.equals(codigo)) {
            return true;
        }
        
        return false;
    }

    /**
     * Armazena um código para um identificador. 
     * Nota: Este método não está na interface mas será usado internamente ou via injeção se necessário.
     */
    public void armazenar(String identificador, String codigo) {
        store.put(identificador, new OtpData(codigo, Instant.now().plus(EXPIRATION)));
        LOGGER.debug("[OTP] Código armazenado para {}", identificador);
    }

    @Override
    public void consumir(String identificador) {
        store.remove(identificador);
    }

    private static class OtpData {
        final String codigo;
        final Instant expiration;

        OtpData(String codigo, Instant expiration) {
            this.codigo = codigo;
            this.expiration = expiration;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiration);
        }
    }
}
