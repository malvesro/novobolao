package com.opendev.bolao.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Indica se existe bolão individual vinculado ao jogo informado.
     *
     * @param jogoId ID do jogo.
     * @return true quando existir vínculo.
     */
    boolean existsByJogoId(Long jogoId);

    /**
     * Retorna os IDs de jogos que possuem vínculo com bolão individual.
     *
     * @param jogoIds IDs de jogos candidatos.
     * @return IDs que possuem vínculo.
     */
    @Query("select b.jogo.id from BolaoIndividual b where b.jogo.id in :jogoIds")
    List<Long> findJogoIdsVinculados(@Param("jogoIds") Collection<Long> jogoIds);
}
