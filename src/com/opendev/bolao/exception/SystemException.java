package com.opendev.bolao.exception;

/**
 * Exceção para erros de infraestrutura ou falhas técnicas inesperadas.
 * Devem ser logadas para análise técnica e exibidas ao usuário como erro genérico.
 */
public class SystemException extends BaseBolaoException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
