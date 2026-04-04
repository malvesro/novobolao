package com.opendev.bolao.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.opendev.bolao.model.Privilegio;
import com.opendev.bolao.model.PrivilegioId;

/**
 * Repositório para a entidade Privilegio.
 * Gerencia os papéis de acesso atribuídos aos participantes.
 * 
 * @see Privilegio
 * @see PrivilegioId
 */
@Repository
public interface PrivilegioRepository extends JpaRepository<Privilegio, PrivilegioId> {

    /**
     * Lista todos os privilégios de um participante.
     * 
     * @param idParticipante ID do participante.
     * @return Lista de privilégios/papéis.
     */
    List<Privilegio> findByIdParticipante(Long idParticipante);
}
