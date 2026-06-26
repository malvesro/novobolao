package com.opendev.bolao.exception;

/**
 * Exceção para erros de regras de negócio.
 * Devem ser tratadas e exibidas ao usuário final como uma mensagem amigável.
 */
public class BusinessException extends BaseBolaoException {
    public enum Code {
        INVALID_INPUT,
        NOT_FOUND,
        DELETE_NOT_ALLOWED,
        CONFLICT,
        UNKNOWN
    }

    private final Code code;

    public BusinessException(String message) {
        super(message);
        this.code = Code.UNKNOWN;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = Code.UNKNOWN;
    }

    public BusinessException(Code code, String message) {
        super(message);
        this.code = code == null ? Code.UNKNOWN : code;
    }

    public BusinessException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code == null ? Code.UNKNOWN : code;
    }

    public Code getCode() {
        return this.code;
    }
}
