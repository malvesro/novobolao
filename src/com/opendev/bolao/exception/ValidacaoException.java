package com.opendev.bolao.exception;

import java.util.List;


public class ValidacaoException extends Exception {
    
    private List erros;
    
    public ValidacaoException(List erros) {
        this.erros = erros;
    }
    
    public List getErros() {
        return this.erros;
    }
    
    public void setErros(List erros) {
        this.erros = erros;
    }

}
