package com.opendev.bolao.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import jakarta.mail.Authenticator;

class EmailSessionConfigurationTest {

    @Test
    void deveConfigurarAutenticacaoQuandoHabilitada() throws Exception {
        Properties settings = new Properties();
        settings.setProperty("mail.smtp.host", "smtp.local");
        settings.setProperty("mail.smtp.port", "2525");
        settings.setProperty("mail.smtp.auth", "true");
        settings.setProperty("mail.smtp.auth.user", "user@local");
        settings.setProperty("mail.smtp.auth.password", "segredo");
        settings.setProperty("mail.smtp.auth.mechanisms", "PLAIN");

        Email.MailContext context = Email.mailContextForTests(settings);

        assertThat(context.properties().getProperty("mail.smtp.host")).isEqualTo("smtp.local");
        assertThat(context.properties().getProperty("mail.smtp.port")).isEqualTo("2525");
        assertThat(context.properties().getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(context.properties().getProperty("mail.smtp.auth.mechanisms")).isEqualTo("PLAIN");
        assertThat(context.authenticator()).isNotNull();
    }

    @Test
    void deveDesabilitarAutenticacaoQuandoNaoConfigurada() throws Exception {
        Properties settings = new Properties();
        settings.setProperty("mail.smtp.host", "smtp.local");
        settings.setProperty("mail.smtp.port", "2525");
        settings.setProperty("mail.smtp.auth", "false");

        Email.MailContext context = Email.mailContextForTests(settings);

        assertThat(context.properties().getProperty("mail.smtp.auth")).isEqualTo("false");
        assertThat(context.authenticator()).isNull();
    }
}
