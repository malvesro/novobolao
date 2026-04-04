package com.opendev.bolao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.opendev.bolao.model.Jogo;
import java.util.Date;
import java.util.List;

/**
 * Repositório para a entidade Jogo.
 * Gerencia todas as operações de persistência relacionadas aos jogos da Copa.
 */
@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {

    /**
     * Busca jogos que já ocorreram (possuem resultado preenchido).
     * 
     * @return Lista de jogos finalizados.
     */
    @Query("SELECT j FROM Jogo j WHERE j.golsEquipe1 IS NOT NULL AND j.golsEquipe2 IS NOT NULL")
    List<Jogo> findJogosFinalizados();

    /**
     * Busca jogos por data específica.
     * 
     * @param data A data do jogo.
     * @return Lista de jogos na data.
     */
    List<Jogo> findByData(Date data);

    /**
     * Busca jogos por data e hora específicas.
     * 
     * @param data A data do jogo.
     * @param hora A hora do jogo.
     * @return Lista de jogos na data e hora.
     */
    List<Jogo> findByDataAndHora(Date data, Date hora);

    /**
     * Retorna a quantidade de jogos que já possuem resultado.
     * 
     * @return Quantidade de jogos.
     */
    @Query("SELECT COUNT(j) FROM Jogo j WHERE j.golsEquipe1 IS NOT NULL AND j.golsEquipe2 IS NOT NULL")
    long countJogosFinalizados();
}
