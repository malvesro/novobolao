package com.opendev.bolao.service;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.service.dto.PalpiteAuthorization;
import org.springframework.security.core.Authentication;

/**
 * Avalia se o participante autenticado pode editar palpites para um determinado jogo.
 */
public interface PalpiteAuthorizationService {

    PalpiteAuthorization avaliar(Authentication authentication, Jogo jogo, Palpite palpiteSelecionado);
}
