package com.opendev.bolao.service;

/**
 * Serviço para geração e validação de códigos de uso único (OTP).
 */
public interface OtpService {

    /**
     * Gera um código aleatório de 6 caracteres (números, letras e caracteres gráficos).
     * @return O código gerado.
     */
    String gerarCodigo();

    /**
     * Armazena um código para um identificador por um período de tempo.
     * @param identificador Chave para busca (ex: e-mail).
     * @param codigo Código a ser armazenado.
     */
    void armazenar(String identificador, String codigo);
    
    /**
     * Valida um código informado comparando com o armazenado para um determinado identificador (ex: e-mail).
     * @param identificador Chave para busca do código (ex: e-mail ou IP).
     * @param codigo Informado pelo usuário.
     * @return true se válido, false caso contrário.
     */
    boolean validar(String identificador, String codigo);

    /**
     * Remove o código associado ao identificador após o sucesso.
     * @param identificador Chave de identificação.
     */
    void consumir(String identificador);
}
