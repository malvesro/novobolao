package com.opendev.bolao.email;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class BrevoPayloadTest {

    @Test
    public void testJsonBuilding() throws Exception {
        EmailMessage message = new EmailMessage(
            "sender@test.com",
            "Sender Name",
            "Test Subject",
            "<html><body>Hello</body></html>",
            Arrays.asList("dest1@test.com", "dest2@test.com")
        );
        
        // Usando reflexão ou tornando o método protegido para testar o buildJson
        // Como o método é privado, vou testar via uma subclasse ou apenas validar que o escape funciona
        BrevoEmailSender sender = new BrevoEmailSender("fake-key");
        
        java.lang.reflect.Method method = BrevoEmailSender.class.getDeclaredMethod("buildJson", EmailMessage.class);
        method.setAccessible(true);
        String json = (String) method.invoke(sender, message);
        
        System.out.println("Generated JSON: " + json);
        
        assertTrue(json.contains("\"email\":\"sender@test.com\""));
        assertTrue(json.contains("\"name\":\"Sender Name\""));
        assertTrue(json.contains("\"subject\":\"Test Subject\""));
        assertTrue(json.contains("\"htmlContent\":\"<html><body>Hello</body></html>\""));
        assertTrue(json.contains("\"email\":\"dest1@test.com\""));
        assertTrue(json.contains("\"email\":\"dest2@test.com\""));
    }

    @Test
    public void testJsonEscape() throws Exception {
        BrevoEmailSender sender = new BrevoEmailSender("fake-key");
        java.lang.reflect.Method method = BrevoEmailSender.class.getDeclaredMethod("escapeJson", String.class);
        method.setAccessible(true);
        
        String escaped = (String) method.invoke(sender, "Hello \"World\"");
        assertTrue(escaped.contains("Hello \\\"World\\\""));
        
        escaped = (String) method.invoke(sender, "Line 1\nLine 2");
        assertTrue(escaped.contains("Line 1\\nLine 2"));
    }
}
