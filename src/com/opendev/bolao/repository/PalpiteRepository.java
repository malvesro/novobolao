package com.opendev.bolao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.PalpiteId;

import java.util.List;

/**
 * Repositório para a entidade Palpite.
 * Gerencia a persistência dos palpites dos participantes, utilizando chave composta.
 */
@Repository
public interface PalpiteRepository extends JpaRepository<Palpite, PalpiteId> {

    /**
     * Busca todos os palpites de um determinado jogo.
     * 
     * @param idJogo ID do jogo.
     * @return Lista de palpites para o jogo.
     */
    List<Palpite> findByIdJogo(Long idJogo);

    /**
     * Busca todos os palpites de um participante identificado pelo seu login.
     * 
     * @param login Login do participante.
     * @return Lista de palpites.
     */
    List<Palpite> findByParticipanteLogin(String login);

    /**
     * Busca um palpite específico de um participante (por login) para um jogo.
     * 
     * @param login Login do participante.
     * @param idJogo ID do jogo.
     * @return O palpite encontrado ou null.
     */
     Palpite findByParticipanteLoginAndIdJogo(String login, Long idJogo);

    /**
     * Busca um palpite específico de um participante para um jogo.
     * 
     * @param participante O participante.
     * @param jogo O jogo.
     * @return O palpite encontrado ou null.
     */
    Palpite findByParticipanteAndJogo(Participante participante, Jogo jogo);

    /**
     * Busca todos os palpites dos participantes listados.
     * 
     * @param participantes Lista de participantes.
     * @return Lista de palpites.
     */
    List<Palpite> findByParticipanteIn(List<Participante> participantes);

    /**
     * Busca palpites de um participante pelo seu ID.
     * 
     * @param idParticipante ID do participante.
     * @return Lista de palpites.
     */
    List<Palpite> findByIdParticipante(Long idParticipante);

    /**
     * Conta a quantidade de palpites vinculados a um jogo.
     *
     * @param idJogo ID do jogo.
     * @return quantidade de palpites.
     */
    long countByIdJogo(Long idJogo);
}
