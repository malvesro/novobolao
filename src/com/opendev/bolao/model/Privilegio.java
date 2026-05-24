package com.opendev.bolao.model;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 * Representa um privilégio (papel/role) de um participante no sistema.
 * Mapeado para a tabela PRI_PRIVILEGIO com chave composta.
 */
@Entity
@Table(name = "PRI_PRIVILEGIO")
@IdClass(PrivilegioId.class)
public class Privilegio implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "PRI_PAR_ID")
	private Long idParticipante;

	@Id
	@Column(name = "PRI_PAPEL", length = 50)
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
