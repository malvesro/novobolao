package com.opendev.bolao.exception;

/**
 * Exceção para erros de regras de negócio.
 * Devem ser tratadas e exibidas ao usuário final como uma mensagem amigável.
 */
public class BusinessException extends BaseBolaoException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
