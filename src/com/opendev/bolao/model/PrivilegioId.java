package com.opendev.bolao.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta para a entidade Privilegio.
 * Necessária para o mapeamento JPA com @IdClass.
 */
public class PrivilegioId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idParticipante;
    private String papel;

    public PrivilegioId() {}

    public PrivilegioId(Long idParticipante, String papel) {
        this.idParticipante = idParticipante;
        this.papel = papel;
    }

    public Long getIdParticipante() {
        return idParticipante;
    }

    public void setIdParticipante(Long idParticipante) {
        this.idParticipante = idParticipante;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegioId that = (PrivilegioId) o;
        return Objects.equals(idParticipante, that.idParticipante) && 
               Objects.equals(papel, that.papel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParticipante, papel);
    }
}
