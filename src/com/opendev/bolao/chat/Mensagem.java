package com.opendev.bolao.chat;

import java.io.Serializable;

import com.opendev.bolao.util.ValidacaoUtils;

public class Mensagem implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String loginParticipante;
	private String texto;
	private String dataHoraEnvio;
	private String apelidoParticipante;

	public String getDataHoraEnvio() {
		return dataHoraEnvio;
	}

	public void setDataHoraEnvio(String dataHoraEnvio) {
		this.dataHoraEnvio = dataHoraEnvio;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLoginParticipante() {
		return loginParticipante;
	}

	public void setLoginParticipante(String loginParticipante) {
		this.loginParticipante = loginParticipante;
	}
	
    public String getApelidoParticipante() {
    	if (ValidacaoUtils.isVazia(this.apelidoParticipante)) {
    		this.apelidoParticipante = getLoginParticipante();
    	}
		return this.apelidoParticipante;
	}

	public void setApelidoParticipante(String apelidoParticipante) {
		this.apelidoParticipante = apelidoParticipante;
	}

	public int compareTo(Object o) {
        Mensagem other = (Mensagem) o;
        if (other != null) {
        	return this.getId().compareTo(other.getId());
        }
        return 0;
    }

}
