package com.opendev.bolao.repository;

import com.opendev.bolao.model.ChatMensagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {

    List<ChatMensagem> findAllByOrderByIdDesc(Pageable pageable);

    List<ChatMensagem> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    @Query("""
            select m
            from ChatMensagem m
            where (:termo is null
                   or lower(m.texto) like lower(concat('%', :termo, '%'))
                   or lower(m.nomeExibicao) like lower(concat('%', :termo, '%'))
                   or lower(m.loginAutor) like lower(concat('%', :termo, '%')))
              and (:autorLogin is null or m.loginAutor = :autorLogin)
              and (:dataInicio is null or m.dataEnvio >= :dataInicio)
              and (:dataFim is null or m.dataEnvio <= :dataFim)
            order by m.id desc
            """)
    Page<ChatMensagem> buscarHistoricoFiltrado(@Param("termo") String termo,
                                               @Param("autorLogin") String autorLogin,
                                               @Param("dataInicio") Date dataInicio,
                                               @Param("dataFim") Date dataFim,
                                               Pageable pageable);
}
