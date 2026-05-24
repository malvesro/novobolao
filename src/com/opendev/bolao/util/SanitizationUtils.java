package com.opendev.bolao.util;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utilitário centralizado para sanitização e validação de entradas controladas pelo usuário.
 * A abordagem prioriza remoção de HTML, normalização Unicode e aplicação de limites de tamanho.
 */
public final class SanitizationUtils {

    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s{2,}");
    private static final Pattern TAGS_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[a-z0-9#]{1,10};", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,32}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,255}$");

    private SanitizationUtils() {
    }

    /**
     * Remove HTML, normaliza Unicode e aplica limite de tamanho.
     */
    public static String cleanText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        String withoutControl = CONTROL_CHARACTERS.matcher(normalized).replaceAll("");
        String collapsedSpaces = MULTIPLE_SPACES.matcher(withoutControl).replaceAll(" ");
        String sanitized = TAGS_PATTERN.matcher(collapsedSpaces).replaceAll("");
        sanitized = HTML_ENTITY_PATTERN.matcher(sanitized).replaceAll("");
        if (maxLength > 0 && sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        return sanitized;
    }

    public static boolean containsHtml(String value) {
        if (value == null) {
            return false;
        }
        String normalizedOriginal = Normalizer.normalize(value, Normalizer.Form.NFKC);
        String sanitized = cleanText(value, normalizedOriginal.length());
        return !Objects.equals(sanitized, normalizedOriginal.trim());
    }

    public static boolean isValidLogin(String login) {
        return login != null && LOGIN_PATTERN.matcher(login).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static void requireSafe(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
