package com.opendev.bolao.email;

import java.util.Date;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementação do envio de e-mail via SMTP utilizando Jakarta Mail.
 */
public class SmtpEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailSender.class);
    private final EmailConfiguration configuration;

    public SmtpEmailSender(EmailConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void enviar(EmailMessage message) throws Exception {
        Properties settings = configuration.asProperties();
        String smtpHost = settings.getProperty("mail.smtp.host");
        
        if (smtpHost == null || smtpHost.trim().isEmpty()) {
            throw new EmailException("Servidor SMTP não configurado (mail.smtp.host).");
        }

        MailContext mailContext = createMailContext(settings);
        Session session = Session.getInstance(mailContext.properties, mailContext.authenticator);
        Message msg = new MimeMessage(session);

        // Destinatários
        String para = String.join(",", message.getPara());
        msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(para, false));

        if (!message.getCc().isEmpty()) {
            String cc = String.join(",", message.getCc());
            msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
        }

        if (!message.getBcc().isEmpty()) {
            String bcc = String.join(",", message.getBcc());
            msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
        }

        // Remetente
        if (message.getDeNome() != null && !message.getDeNome().isEmpty()) {
            msg.setFrom(new InternetAddress(message.getDe(), message.getDeNome(), "UTF-8"));
        } else {
            msg.setFrom(new InternetAddress(message.getDe()));
        }

        msg.setSubject(message.getAssunto());
        msg.setHeader("X-Mailer", "BOLAO DE PLACA");
        msg.setSentDate(new Date());
        msg.setContent(message.getConteudo(), "text/html; charset=UTF-8");

        LOGGER.info("[SMTP] Enviando e-mail via host={} para={}", smtpHost, para);
        Transport.send(msg);
        LOGGER.info("[SMTP] E-mail enviado com sucesso.");
    }

    private static MailContext createMailContext(Properties settings) throws EmailException {
        Properties propriedadesDeEnvio = new Properties();
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.host");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.port");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.starttls.enable");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.starttls.required");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.ssl.enable");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.ssl.trust");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.connectiontimeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.timeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.writetimeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.auth.mechanisms");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.sasl.enable");

        boolean usarAutenticacao = Boolean.parseBoolean(settings.getProperty("mail.smtp.auth", "false"));
        Authenticator auth = null;
        if (usarAutenticacao) {
            final String usuario = settings.getProperty("mail.smtp.auth.user");
            final String senha = settings.getProperty("mail.smtp.auth.password");
            if (usuario == null || usuario.trim().isEmpty()) {
                throw new EmailException("Usuário SMTP não informado (mail.smtp.auth.user).");
            }
            propriedadesDeEnvio.setProperty("mail.smtp.auth", "true");
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, senha == null ? "" : senha);
                }
            };
        } else {
            propriedadesDeEnvio.setProperty("mail.smtp.auth", "false");
        }

        return new MailContext(propriedadesDeEnvio, auth);
    }

    private static void copyIfPresent(Properties source, Properties target, String key) {
        String value = source.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            target.setProperty(key, value.trim());
        }
    }

    static MailContext createMailContextForTests(Properties settings) throws EmailException {
        return createMailContext(settings);
    }

    static class MailContext {
        final Properties properties;
        final Authenticator authenticator;
        MailContext(Properties properties, Authenticator authenticator) {
            this.properties = properties;
            this.authenticator = authenticator;
        }
    }
}
