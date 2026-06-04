package com.opendev.bolao.email;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

final class EmailConfiguration {

    private static final String BUNDLE_NAME = "com.opendev.bolao.email.email";
    private static final String EXTERNAL_CONFIG_ENV = "BOLAO_EMAIL_CONFIG";
    private static final String EXTERNAL_CONFIG_PROPERTY = "bolao.email.config";

    private static final Map<String, String> ENVIRONMENT_MAPPING;

    static {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("SMTP_HOST", "mail.smtp.host");
        mapping.put("SMTP_PORT", "mail.smtp.port");
        mapping.put("SMTP_AUTH", "mail.smtp.auth");
        mapping.put("SMTP_USERNAME", "mail.smtp.auth.user");
        mapping.put("SMTP_PASSWORD", "mail.smtp.auth.password");
        mapping.put("SMTP_TLS", "mail.smtp.starttls.enable");
        mapping.put("SMTP_STARTTLS_REQUIRED", "mail.smtp.starttls.required");
        mapping.put("SMTP_SSL", "mail.smtp.ssl.enable");
        mapping.put("SMTP_SSL_TRUST", "mail.smtp.ssl.trust");
        mapping.put("SMTP_CONNECTION_TIMEOUT", "mail.smtp.connectiontimeout");
        mapping.put("SMTP_TIMEOUT", "mail.smtp.timeout");
        mapping.put("SMTP_WRITE_TIMEOUT", "mail.smtp.writetimeout");
        mapping.put("SMTP_FROM_ADDRESS", "mail.from.address");
        mapping.put("SMTP_FROM_NAME", "mail.from.name");
        mapping.put("SMTP_SYSTEM_URL", "mail.property.systemurl");
        mapping.put("SMTP_ADMIN_EMAILS", "mail.admin.emails");
        mapping.put("EMAIL_PROVIDER", "mail.provider");
        mapping.put("CHAVE_API_BREVO", "mail.brevo.api.key");
        ENVIRONMENT_MAPPING = Collections.unmodifiableMap(mapping);
    }

    private final Properties properties;

    private EmailConfiguration(Properties properties) {
        this.properties = properties;
    }

    static EmailConfiguration load() {
        Properties properties = new Properties();
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        for (String key : bundle.keySet()) {
            properties.setProperty(key, bundle.getString(key));
        }

        overlayExternalFile(properties);
        overlayEnvironment(properties);
        overlaySystemOverrides(properties);

        return new EmailConfiguration(properties);
    }

    Properties asProperties() {
        Properties copy = new Properties();
        copy.putAll(this.properties);
        return copy;
    }

    String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Retorna a lista de e-mails dos administradores que devem receber
     * notificações de novos cadastros. Lidos da variável SMTP_ADMIN_EMAILS
     * (separados por vírgula).
     */
    String[] getAdminEmails() {
        String value = this.properties.getProperty("mail.admin.emails", "").trim();
        if (value.isEmpty()) {
            return new String[0];
        }
        String[] emails = value.split(",");
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String email : emails) {
            String trimmed = email.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result.toArray(new String[0]);
    }

    private static void overlayExternalFile(Properties properties) {
        String path = firstNonBlank(
                System.getenv(EXTERNAL_CONFIG_ENV),
                System.getProperty(EXTERNAL_CONFIG_ENV),
                System.getProperty(EXTERNAL_CONFIG_PROPERTY)
        );

        if (path == null) {
            return;
        }

        Path externalPath = Path.of(path);
        if (!Files.exists(externalPath)) {
            throw new IllegalStateException("Arquivo de configuração SMTP não encontrado: " + externalPath);
        }

        Properties external = new Properties();
        try (InputStream inputStream = Files.newInputStream(externalPath)) {
            external.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao carregar configuração SMTP externa: " + externalPath, ex);
        }

        external.forEach((key, value) -> {
            String normalizedKey = Objects.toString(key, "").trim();
            String normalizedValue = Objects.toString(value, "").trim();
            if (!normalizedKey.isEmpty() && !normalizedValue.isEmpty()) {
                properties.setProperty(normalizedKey, normalizedValue);
            }
        });
    }

    private static void overlayEnvironment(Properties properties) {
        for (Map.Entry<String, String> entry : ENVIRONMENT_MAPPING.entrySet()) {
            String value = firstNonBlank(
                    System.getenv(entry.getKey()),
                    System.getProperty(entry.getKey())
            );
            if (value != null) {
                properties.setProperty(entry.getValue(), value.trim());
            }
        }
    }

    private static void overlaySystemOverrides(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String override = System.getProperty(key);
            if (override != null && !override.isBlank()) {
                properties.setProperty(key, override.trim());
            }
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }
}
