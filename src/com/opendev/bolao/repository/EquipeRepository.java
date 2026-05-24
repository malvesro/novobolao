package com.opendev.bolao.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Retorna todas as equipes que não pertencem ao grupo de placeholders.
     * Filtra explicitamente pelos grupos de A a L da Copa 2026.
     * Exclui qualquer equipe que tenha nomes começando com números ou 'V' ou 'Winner' para evitar placeholders.
     *
     * @return Lista de equipes reais ordenada por nome.
     */
    @Query("SELECT e FROM Equipe e WHERE e.grupo IN ('A','B','C','D','E','F','G','H','I','J','K','L') " +
           "AND e.nomePais NOT LIKE '1%' " +
           "AND e.nomePais NOT LIKE '2%' " +
           "AND e.nomePais NOT LIKE '3%' " +
           "AND e.nomePais NOT LIKE 'V%' " +
           "AND e.nomePais NOT LIKE 'Winner%' " +
           "ORDER BY e.nomePais ASC")
    List<Equipe> buscarApenasPaisesReais();
}
