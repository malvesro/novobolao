package com.opendev.bolao.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.opendev.bolao.model.BolaoIndividual;

/**
 * Repositório para a entidade BolaoIndividual.
 * Gerencia os bolões do tipo "vaca" vinculados a partidas específicas.
 * 
 * @see BolaoIndividual
 */
@Repository
public interface BolaoIndividualRepository extends JpaRepository<BolaoIndividual, Long> {

    /**
     * Busca um bolão individual pelo jogo vinculado.
     * 
     * @param jogoId ID do jogo.
     * @return Um Optional contendo o bolão se encontrado.
     */
    Optional<BolaoIndividual> findByJogoId(Long jogoId);
}
