package com.opendev.bolao.repository;

import com.opendev.bolao.model.ChatMensagem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {

    List<ChatMensagem> findAllByOrderByIdDesc(Pageable pageable);

    List<ChatMensagem> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}
