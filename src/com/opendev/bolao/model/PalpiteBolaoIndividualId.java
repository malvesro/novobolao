package com.opendev.bolao.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta para a entidade PalpiteBolaoIndividual.
 * Necessária para o mapeamento JPA com @IdClass.
 */
public class PalpiteBolaoIndividualId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long participanteId;
    private Long bolaoIndividualId;

    public PalpiteBolaoIndividualId() {}

    public PalpiteBolaoIndividualId(Long participanteId, Long bolaoIndividualId) {
        this.participanteId = participanteId;
        this.bolaoIndividualId = bolaoIndividualId;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }

    public Long getBolaoIndividualId() {
        return bolaoIndividualId;
    }

    public void setBolaoIndividualId(Long bolaoIndividualId) {
        this.bolaoIndividualId = bolaoIndividualId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PalpiteBolaoIndividualId that = (PalpiteBolaoIndividualId) o;
        return Objects.equals(participanteId, that.participanteId) && 
               Objects.equals(bolaoIndividualId, that.bolaoIndividualId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participanteId, bolaoIndividualId);
    }
}
