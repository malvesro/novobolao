package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Timestamp;


public class PalpiteBolaoIndividual implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long bolaoIndividualId;
    private Long participanteId;
    private boolean pago;
    private Integer golsEquipe1;
    private Integer golsEquipe2;
    private String ip;
    private Timestamp dataHoraPagamento;
    private BolaoIndividual bolaoIndividual;
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
