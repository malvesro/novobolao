package com.opendev.bolao.email;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementação do envio de e-mail utilizando a API REST do Brevo.
 */
public class BrevoEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoEmailSender.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    
    private final String apiKey;
    private final HttpClient httpClient;

    public BrevoEmailSender(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void enviar(EmailMessage message) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new EmailException("Chave da API do Brevo não configurada!");
        }

        String jsonPayload = buildJson(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_API_URL))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        LOGGER.info("[BREVO] Enviando e-mail via API REST - de: {} | para: {} | assunto: {}", 
                message.getDe(),
                message.getPara().stream().collect(Collectors.joining(", ")),
                message.getAssunto());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            LOGGER.info("[BREVO] E-mail enviado com sucesso. Resposta: {}", response.body());
        } else {
            LOGGER.error("[BREVO] Falha ao enviar e-mail. Status: {}, Erro: {}", 
                    response.statusCode(), response.body());
            throw new EmailException("Erro ao enviar e-mail via Brevo API: " + response.body());
        }
    }

    private String buildJson(EmailMessage message) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Sender
        json.append("\"sender\":{");
        json.append("\"email\":\"").append(escapeJson(message.getDe())).append("\",");
        if (message.getDeNome() != null && !message.getDeNome().isEmpty()) {
            json.append("\"name\":\"").append(escapeJson(message.getDeNome())).append("\"");
        } else {
            json.append("\"name\":\"").append(escapeJson(message.getDe())).append("\"");
        }
        json.append("},");
        
        // Subject
        json.append("\"subject\":\"").append(escapeJson(message.getAssunto())).append("\",");
        
        // Content
        json.append("\"htmlContent\":\"").append(escapeJson(message.getConteudo())).append("\",");
        
        // To
        json.append("\"to\":[");
        for (int i = 0; i < message.getPara().size(); i++) {
            json.append("{\"email\":\"").append(escapeJson(message.getPara().get(i))).append("\"}");
            if (i < message.getPara().size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        // CC (Omitido por simplicidade se vazio, Brevo aceita se presente)
        if (!message.getCc().isEmpty()) {
            json.append(",\"cc\":[");
            for (int i = 0; i < message.getCc().size(); i++) {
                json.append("{\"email\":\"").append(escapeJson(message.getCc().get(i))).append("\"}");
                if (i < message.getCc().size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
        }

        // BCC
        if (!message.getBcc().isEmpty()) {
            json.append(",\"bcc\":[");
            for (int i = 0; i < message.getBcc().size(); i++) {
                json.append("{\"email\":\"").append(escapeJson(message.getBcc().get(i))).append("\"}");
                if (i < message.getBcc().size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
