package com.opendev.bolao.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.opendev.bolao.model.Participante;

/**
 * Repositório para a entidade Participante.
 * Gerencia operações de CRUD e consultas customizadas utilizando Spring Data JPA.
 * 
 * @see Participante
 */
@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    /**
     * Busca um participante pelo seu login único.
     * 
     * @param login O login do participante.
     * @return Um Optional contendo o participante se encontrado.
     */
    Optional<Participante> findByLogin(String login);

    /**
     * Busca um participante pelo seu e-mail.
     * 
     * @param email O e-mail do participante.
     * @return Um Optional contendo o participante se encontrado.
     */
    Optional<Participante> findByEmail(String email);
}
