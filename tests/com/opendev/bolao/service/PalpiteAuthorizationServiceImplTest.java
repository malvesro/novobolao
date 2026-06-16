package com.opendev.bolao.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Time;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import com.opendev.bolao.util.BolaoTime;

class PalpiteAuthorizationServiceImplTest {

    private Clock clock;
    private PalpiteAuthorizationServiceImpl service;
    private ZoneId zoneId;

    @BeforeEach
    void setUp() {
        // Os testes seguem o timezone canônico do domínio para evitar suposições
        // implícitas de UTC após a padronização temporal.
        zoneId = BolaoTime.getZoneId();
        clock = Clock.fixed(Instant.parse("2026-03-01T15:00:00Z"), zoneId);
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

    @Test
    @DisplayName("Deve bloquear admin para registro de palpite")
    void deveBloquearAdminParaPalpite() {
        Jogo jogo = criarJogo(ZonedDateTime.now(clock).plusHours(3));
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isFalse();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.LOCKED);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.ADMIN_RESTRICTED);
    }

    @Test
    @DisplayName("Deve permitir palpite as 18:33 para jogo as 22:00 no mesmo dia (java.sql.Date)")
    void devePermitirPalpiteComSqlDateSemDerivaDeTimezone() {
        Clock clockBrt1833 = Clock.fixed(Instant.parse("2026-06-16T21:33:00Z"), zoneId);
        service = new PalpiteAuthorizationServiceImpl(clockBrt1833);
        Jogo jogo = new Jogo();
        jogo.setData(Date.valueOf("2026-06-16"));
        jogo.setHora(Time.valueOf("22:00:00"));
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isTrue();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.PENDING);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.NONE);
    }

    @Test
    @DisplayName("Deve normalizar horario quando Time.toLocalTime divergir do valor canônico")
    void deveNormalizarHoraQuandoToLocalTimeDivergir() {
        Clock clockBrt1833 = Clock.fixed(Instant.parse("2026-06-16T21:33:00Z"), zoneId);
        service = new PalpiteAuthorizationServiceImpl(clockBrt1833);
        long millisHora22 = ZonedDateTime.of(1970, 1, 1, 22, 0, 0, 0, zoneId).toInstant().toEpochMilli();
        Time horaDivergente = new Time(millisHora22) {
            @Override
            public LocalTime toLocalTime() {
                return LocalTime.of(1, 0);
            }
        };

        Jogo jogo = new Jogo();
        jogo.setData(Date.valueOf("2026-06-16"));
        jogo.setHora(horaDivergente);
        Authentication authentication = autenticacao(true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        PalpiteAuthorization resultado = service.avaliar(authentication, jogo, null);

        assertThat(resultado.isPermitido()).isTrue();
        assertThat(resultado.getStatus()).isEqualTo(PalpiteAuthorization.Status.PENDING);
        assertThat(resultado.getReason()).isEqualTo(PalpiteAuthorization.RejectionReason.NONE);
    }

    private Authentication autenticacao(boolean autenticado, List<? extends GrantedAuthority> authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("user", "credentials", authorities);
        token.setAuthenticated(autenticado);
        return token;
    }

    private Jogo criarJogo(ZonedDateTime dataHora) {
        Jogo jogo = new Jogo();
        ZonedDateTime ajustado = dataHora.withZoneSameInstant(zoneId);
        jogo.setData(java.util.Date.from(ajustado.toInstant()));
        jogo.setHora(Time.valueOf(ajustado.toLocalTime()));
        return jogo;
    }
}
