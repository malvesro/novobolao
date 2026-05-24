package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;

/**
 * Representa um palpite em um bolão individual.
 * Mapeado para a tabela PAI_PALPITE_INDIVIDUAL com chave composta.
 */
@Entity
@Table(name = "PAI_PALPITE_INDIVIDUAL")
@IdClass(PalpiteBolaoIndividualId.class)
public class PalpiteBolaoIndividual implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PAI_BOI_ID")
	private Long bolaoIndividualId;

	@Id
	@Column(name = "PAI_PAR_ID")
    private Long participanteId;

	@Column(name = "PAI_PAR_PAGO", nullable = false)
    private boolean pago;

	@Column(name = "PAI_EQP1_GOLS", nullable = false)
    private Integer golsEquipe1;

	@Column(name = "PAI_EQP2_GOLS", nullable = false)
    private Integer golsEquipe2;

	@Column(name = "PAL_IP", nullable = false, length = 45)
    private String ip;

	@Column(name = "PAI_DH_PAGTO")
    private Timestamp dataHoraPagamento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PAI_BOI_ID", insertable = false, updatable = false)
    private BolaoIndividual bolaoIndividual;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PAI_PAR_ID", insertable = false, updatable = false)
    private Participante participante;
    
    public BolaoIndividual getBolaoIndividual() {
        return this.bolaoIndividual;
    }
    
    public void setBolaoIndividual(BolaoIndividual bolaoIndividual) {
        this.bolaoIndividual = bolaoIndividual;
    }
    
    public Long getBolaoIndividualId() {
        return this.bolaoIndividualId;
    }
    
    public void setBolaoIndividualId(Long bolaoIndividualId) {
        this.bolaoIndividualId = bolaoIndividualId;
    }
    
    public Timestamp getDataHoraPagamento() {
        return this.dataHoraPagamento;
    }
    
    public void setDataHoraPagamento(Timestamp dataHoraPagamento) {
        this.dataHoraPagamento = dataHoraPagamento;
    }
    
    public Integer getGolsEquipe1() {
        return this.golsEquipe1;
    }
    
    public void setGolsEquipe1(Integer golsEquipe1) {
        this.golsEquipe1 = golsEquipe1;
    }
    
    public Integer getGolsEquipe2() {
        return this.golsEquipe2;
    }
    
    public void setGolsEquipe2(Integer golsEquipe2) {
        this.golsEquipe2 = golsEquipe2;
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public boolean isPago() {
        return this.pago;
    }
    
    public void setPago(boolean pago) {
        this.pago = pago;
    }
    
    public Participante getParticipante() {
        return this.participante;
    }
    
    public void setParticipante(Participante participante) {
        this.participante = participante;
    }
    
    public Long getParticipanteId() {
        return this.participanteId;
    }
    
    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }
    
    
}
