package com.opendev.bolao.exception;

/**
 * Exceção base para todas as exceções de negócio e sistema do Bolão.
 * Deve ser estendida por todas as exceções customizadas.
 */
public abstract class BaseBolaoException extends RuntimeException {
    public BaseBolaoException(String message) {
        super(message);
    }

    public BaseBolaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
