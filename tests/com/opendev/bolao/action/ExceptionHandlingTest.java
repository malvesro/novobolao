package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.opendev.bolao.exception.SystemException;
import com.opendev.bolao.service.ParticipanteService;

class ExceptionHandlingTest {

    @Test
    @DisplayName("ParticipanteAction deve relancar SystemException")
    void deveRelancarSystemExceptionEmErroInesperado() throws com.opendev.bolao.exception.ValidacaoException {
        ParticipanteService participanteService = mock(ParticipanteService.class);
        org.mockito.Mockito.doThrow(new RuntimeException("Falha catastrófica!"))
                .when(participanteService).alterarSenha("user", "old", "new");

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        
        // Simula input para disparar o método
        action.setSenhaAtual("old");
        action.setNovaSenha("new");
        action.setConfirmarSenha("new");
        
        // Simula login para o teste
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        when(auth.getPrincipal()).thenReturn("user");
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        // O teste verifica se a SystemException é lançada
        // Isso confirma que o handler global do Struts (configurado no struts.xml)
        // receberá a exceção para redirecionar para a página de erro.
        try {
            action.alterarSenha();
            assertThat(true).as("Deveria ter lançado SystemException").isFalse();
        } catch (SystemException e) {
            assertThat(e.getMessage()).isEqualTo("Erro inesperado ao processar a troca de senha.");
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}

