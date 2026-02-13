package com.opendev.bolao.model;

import java.io.Serializable;

public class Privilegio implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private Long idParticipante;
	private String papel;

	public Long getIdParticipante() {
		return idParticipante;
	}

	public void setIdParticipante(Long loginParticipante) {
		this.idParticipante = loginParticipante;
	}

	public String getPapel() {
		return papel;
	}

	public void setPapel(String papel) {
		this.papel = papel;
	}

}
