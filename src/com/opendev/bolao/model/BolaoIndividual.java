package com.opendev.bolao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.*;

/**
 * Representa um bolão individual vinculado a um jogo específico.
 * Mapeado para a tabela BOI_BOLAO_INDIVIDUAL.
 */
@Entity
@Table(name = "BOI_BOLAO_INDIVIDUAL")
public class BolaoIndividual implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BOI_ID")
	private Long id;

	@Column(name = "BOI_VALOR_COTA", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCota;

	@Column(name = "BOI_STATUS", nullable = false)
    private int status;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BOI_JOG_ID")
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
