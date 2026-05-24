package com.opendev.bolao.service.impl;

import java.util.Optional;

import com.opendev.bolao.model.Participante;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.service.dto.ResultadoTrocaSenha;
import com.opendev.bolao.service.dto.SolicitacaoOtp;
import com.opendev.bolao.service.dto.ValidacaoOtp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RecuperacaoSenhaServiceImplTest {

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RecuperacaoSenhaServiceImpl.OtpStore otpStore;

    @InjectMocks
    private RecuperacaoSenhaServiceImpl recuperacaoSenhaService;

    @BeforeEach
    void setUp() {
        // OtpStore receives lenient behavior for store mock if we mock it, 
        // but we're mostly testing the service layer workflow
    }

    @Test
    void solicitarOtp_deveRetornarFalsoSeEmailInvalido() {
        SolicitacaoOtp resultado = recuperacaoSenhaService.solicitarOtp("invalido@", "127.0.0.1", "JUnit");
        assertFalse(resultado.isEnviado());
    }

    @Test
    void solicitarOtp_deveRetornarVerdadeiroComoFakeSuccessSeEmailNaoExiste() {
        when(participanteRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        SolicitacaoOtp resultado = recuperacaoSenhaService.solicitarOtp("naocadastrado@teste.com", "127.0.0.1", "JUnit");

        // Deve retornar TRUE para evitar exploração (User enumeration)
        assertTrue(resultado.isEnviado());
        assertNotNull(resultado.getMensagemNeutra());
    }

    @Test
    void redefinirSenha_deveRetornarFalsoSeOtpInvalido() {
        when(otpStore.consume(anyString(), anyString())).thenReturn(false);

        ResultadoTrocaSenha resultado = recuperacaoSenhaService.redefinirSenha("teste@teste.com", "123456", "NovaSenha123!", "127.0.0.1");

        assertFalse(resultado.isSucesso());
        assertEquals("Código inválido ou expirado.", resultado.getMensagem());
    }

    @Test
    void validarOtp_deveRetornarFalsoSeOtpStoreNegar() {
        when(otpStore.validate(anyString(), anyString())).thenReturn(false);

        ValidacaoOtp resultado = recuperacaoSenhaService.validarOtp("teste@teste.com", "654321", "127.0.0.1");
        assertFalse(resultado.isValido());
    }
}
