package com.opendev.bolao.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.opendev.bolao.model.Equipe;

/**
 * Repositório para a entidade Equipe.
 * Gerencia as seleções nacionais participantes do torneio.
 * 
 * @see Equipe
 */
@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    /**
     * Busca uma equipe pelo nome exato do país.
     * 
     * @param nomePais Nome do país.
     * @return Um Optional contendo a equipe se encontrada.
     */
    Optional<Equipe> findByNomePais(String nomePais);

    /**
     * Retorna as equipes pertencentes a um determinado grupo.
     * 
     * @param grupo Letra do grupo (ex: 'A', 'B').
     * @return Lista de equipes do grupo.
     */
    List<Equipe> findByGrupoOrderByNomePaisAsc(String grupo);
}
