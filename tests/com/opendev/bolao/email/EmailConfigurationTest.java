package com.opendev.bolao.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EmailConfigurationTest {

    private Path configFile;

    @AfterEach
    void cleanup() throws Exception {
        if (configFile != null) {
            Files.deleteIfExists(configFile);
        }
        System.clearProperty("bolao.email.config");
        System.clearProperty("SMTP_HOST");
        System.clearProperty("SMTP_PORT");
        System.clearProperty("SMTP_TLS");
        System.clearProperty("mail.smtp.timeout");
    }

    @Test
    void devePriorizarArquivoExternoESistema() throws Exception {
        Properties base = new Properties();
        base.setProperty("mail.smtp.host", "config.local");
        base.setProperty("mail.smtp.port", "25");
        base.setProperty("mail.smtp.starttls.enable", "false");
        base.setProperty("mail.property.systemurl", "http://localhost:8080/bolao");
        configFile = Files.createTempFile("email-config", ".properties");
        try (OutputStream output = Files.newOutputStream(configFile)) {
            base.store(output, "base");
        }
        System.setProperty("bolao.email.config", configFile.toString());
        System.setProperty("SMTP_HOST", "env.local");
        System.setProperty("SMTP_PORT", "2525");
        System.setProperty("SMTP_TLS", "true");
        System.setProperty("SMTP_TIMEOUT", "15000");
        System.setProperty("mail.smtp.timeout", "15000");

        EmailConfiguration configuration = EmailConfiguration.load();

        assertThat(configuration.getProperty("mail.smtp.host")).isEqualTo("env.local");
        assertThat(configuration.getProperty("mail.smtp.port")).isEqualTo("2525");
        assertThat(configuration.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
        assertThat(configuration.getProperty("mail.smtp.timeout")).isEqualTo("15000");
        assertThat(configuration.getProperty("mail.property.systemurl")).isEqualTo("http://localhost:8080/bolao");
    }
}
