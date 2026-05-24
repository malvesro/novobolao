package com.opendev.bolao.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.opendev.bolao.model.PalpiteBolaoIndividual;
import com.opendev.bolao.model.PalpiteBolaoIndividualId;

/**
 * Repositório para a entidade PalpiteBolaoIndividual.
 * Gerencia palpites em bolões individuais com chave composta.
 * 
 * @see PalpiteBolaoIndividual
 * @see PalpiteBolaoIndividualId
 */
@Repository
public interface PalpiteBolaoIndividualRepository extends JpaRepository<PalpiteBolaoIndividual, PalpiteBolaoIndividualId> {

    /**
     * Lista palpites de um participante em bolões individuais.
     * 
     * @param participanteId ID do participante.
     * @return Lista de palpites.
     */
    List<PalpiteBolaoIndividual> findByParticipanteId(Long participanteId);

    /**
     * Lista palpites vinculados a um bolão individual específico.
     * 
     * @param bolaoIndividualId ID do bolão.
     * @return Lista de palpites realizados no bolão.
     */
    List<PalpiteBolaoIndividual> findByBolaoIndividualId(Long bolaoIndividualId);
}
