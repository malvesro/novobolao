package com.opendev.bolao.email;

/**
 * Interface que define o contrato para envio de e-mails.
 * Permite abstrair o mecanismo de transporte (SMTP, API REST, etc).
 */
public interface EmailSender {

    /**
     * Envia uma mensagem de e-mail.
     * 
     * @param message Dados do e-mail.
     * @throws Exception Caso ocorra erro no envio.
     */
    void enviar(EmailMessage message) throws Exception;
}
