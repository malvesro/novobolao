package com.opendev.bolao.model;

import java.io.Serializable;
import java.math.BigDecimal;


public class BolaoIndividual implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private Long id;
    private BigDecimal valorCota;
    private int status;
    private Jogo jogo;
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Jogo getJogo() {
        return this.jogo;
    }
    
    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }
    
    public int getStatus() {
        return this.status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public BigDecimal getValorCota() {
        return this.valorCota;
    }
    
    public void setValorCota(BigDecimal valorCota) {
        this.valorCota = valorCota;
    }

}
