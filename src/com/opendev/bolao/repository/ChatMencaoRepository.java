package com.opendev.bolao.repository;

import com.opendev.bolao.model.ChatMencao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatMencaoRepository extends JpaRepository<ChatMencao, Long> {

    boolean existsByDestinatarioLoginAndChatMensagemId(String destinatarioLogin, Long chatMensagemId);

    long countByDestinatarioLoginAndDataConfirmacaoIsNull(String destinatarioLogin);

    long countByDestinatarioLogin(String destinatarioLogin);

    List<ChatMencao> findByDestinatarioLoginAndDataConfirmacaoIsNullOrderByIdAsc(String destinatarioLogin, Pageable pageable);

    List<ChatMencao> findByDestinatarioLoginAndChatMensagemIdIn(String destinatarioLogin, Collection<Long> chatMensagemIds);

    List<ChatMencao> findByDestinatarioLoginOrderByIdDesc(String destinatarioLogin, Pageable pageable);

    List<ChatMencao> findByDestinatarioLoginOrderByIdAsc(String destinatarioLogin, Pageable pageable);
}
