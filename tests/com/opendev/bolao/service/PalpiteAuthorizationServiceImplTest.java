package com.opendev.bolao.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.service.dto.PalpiteAuthorization;
import com.opendev.bolao.service.impl.PalpiteAuthorizationServiceImpl;

class PalpiteAuthorizationServiceImplTest {

    private Clock clock;
    private PalpiteAuthorizationServiceImpl service;
    private ZoneId zoneId;

    @BeforeEach
    void setUp() {
        zoneId = ZoneId.of("UTC");
        clock = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), zoneId);
        service = new PalpiteAuthorizationServiceImpl(clock);
    }

    @Test
    @DisplayName("Deve permitir edicao quando usuario possui ROLE_USER e jogo esta no futuro")
    void devePermitirEdicaoParaUsuario() {
        Jogo jogo = criarJogo(ZonedDateTime.now(clock).plusHours(3));
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isTrue();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.PENDING);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.NONE);
    }

    @Test
    @DisplayName("Deve tratar autoridade sem prefixo ROLE_")
    void deveNormalizarRoleSemPrefixo() {
        Jogo jogo = criarJogo(ZonedDateTime.now(clock).plusHours(2));
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("USER")));

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isTrue();
    }

    @Test
    @DisplayName("Deve bloquear quando janela de edicao estiver encerrada, mas manter status registered")
    void deveBloquearPorJanelaEncerrada() {
        Jogo jogo = criarJogo(ZonedDateTime.now(clock).plusMinutes(30));
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Palpite palpite = new Palpite();
        palpite.setGolsEquipe1(1);
        palpite.setGolsEquipe2(0);

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, palpite);

        assertThat(resultado.isPermitido()).isFalse();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.REGISTERED);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.TIME_WINDOW);
    }

    @Test
    @DisplayName("Deve bloquear quando usuario nao possuir papel valido")
    void deveBloquearSemPapel() {
        Jogo jogo = criarJogo(ZonedDateTime.now(clock).plusHours(2));
        Authentication authentication = autenticacao(true, List.of());

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isFalse();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.LOCKED);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.ROLE_MISSING);
    }

    private Authentication autenticacao(boolean autenticado, List<? extends GrantedAuthority> authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("user", "credentials", authorities);
        token.setAuthenticated(autenticado);
        return token;
    }

    private Jogo criarJogo(ZonedDateTime dataHora) {
        Jogo jogo = new Jogo();
        ZonedDateTime ajustado = dataHora.withZoneSameInstant(zoneId);
        jogo.setData(Date.from(ajustado.toInstant()));
        jogo.setHora(Time.valueOf(ajustado.toLocalTime()));
        return jogo;
    }
}
