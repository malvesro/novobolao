package com.opendev.bolao.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta para a entidade Palpite.
 * Necessária para o mapeamento JPA com @IdClass.
 */
public class PalpiteId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idParticipante;
    private Long idJogo;

    public PalpiteId() {}

    public PalpiteId(Long idParticipante, Long idJogo) {
        this.idParticipante = idParticipante;
        this.idJogo = idJogo;
    }

    public Long getIdParticipante() {
        return idParticipante;
    }

    public void setIdParticipante(Long idParticipante) {
        this.idParticipante = idParticipante;
    }

    public Long getIdJogo() {
        return idJogo;
    }

    public void setIdJogo(Long idJogo) {
        this.idJogo = idJogo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PalpiteId palpiteId = (PalpiteId) o;
        return Objects.equals(idParticipante, palpiteId.idParticipante) && 
               Objects.equals(idJogo, palpiteId.idJogo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParticipante, idJogo);
    }
}
