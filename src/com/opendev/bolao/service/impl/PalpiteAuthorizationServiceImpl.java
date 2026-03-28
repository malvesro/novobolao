package com.opendev.bolao.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.service.PalpiteAuthorizationService;
import com.opendev.bolao.service.dto.PalpiteAuthorization;

public class PalpiteAuthorizationServiceImpl implements PalpiteAuthorizationService {

    private final Clock clock;
    private final ZoneId zoneId;

    public PalpiteAuthorizationServiceImpl() {
        this(Clock.systemDefaultZone());
    }

    public PalpiteAuthorizationServiceImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.zoneId = clock.getZone();
    }

    @Override
    public PalpiteAuthorization avaliar(Authentication authentication, Jogo jogo, Palpite palpiteSelecionado) {
        boolean possuiPerfil = possuiAlgumPapel(authentication, "ROLE_USER", "USER", "ROLE_ADMIN", "ADMIN");
        boolean janelaAberta = jogo != null && estaDentroDaJanela(jogo);
        boolean permitido = possuiPerfil && janelaAberta;

        PalpiteAuthorization.Status status = determinarStatus(palpiteSelecionado, permitido);

        if (permitido) {
            return PalpiteAuthorization.permitido(status);
        }

        PalpiteAuthorization.RejectionReason motivo;
        if (!possuiPerfil) {
            motivo = PalpiteAuthorization.RejectionReason.ROLE_MISSING;
        } else if (!janelaAberta) {
            motivo = PalpiteAuthorization.RejectionReason.TIME_WINDOW;
        } else {
            motivo = PalpiteAuthorization.RejectionReason.UNKNOWN;
        }
        return PalpiteAuthorization.negado(status, motivo);
    }

    private PalpiteAuthorization.Status determinarStatus(Palpite palpiteSelecionado, boolean permitido) {
        if (palpiteSelecionado != null) {
            return PalpiteAuthorization.Status.REGISTERED;
        }
        if (permitido) {
            return PalpiteAuthorization.Status.PENDING;
        }
        return PalpiteAuthorization.Status.LOCKED;
    }

    private boolean possuiAlgumPapel(Authentication authentication, String... expectedRoles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return Stream.of(expectedRoles)
                .filter(Objects::nonNull)
                .map(this::normalizarRole)
                .anyMatch(roleMatches(authorities));
    }

    private Predicate<String> roleMatches(Collection<? extends GrantedAuthority> authorities) {
        return normalizedRole -> authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(this::normalizarRole)
                .anyMatch(normalizedRole::equals);
    }

    private String normalizarRole(String role) {
        if (role == null) {
            return "";
        }
        String trimmed = role.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (upper.startsWith("ROLE_")) {
            return upper;
        }
        return "ROLE_" + upper;
    }

    private boolean estaDentroDaJanela(Jogo jogo) {
        ZonedDateTime dataHoraJogo = extrairDataHoraJogo(jogo);
        if (dataHoraJogo == null) {
            return false;
        }
        Instant limite = dataHoraJogo.toInstant();
        Instant agoraMaisUmaHora = clock.instant().plusSeconds(3600);
        return agoraMaisUmaHora.isBefore(limite);
    }

    private ZonedDateTime extrairDataHoraJogo(Jogo jogo) {
        if (jogo == null) {
            return null;
        }
        java.util.Date data = jogo.getData();
        java.sql.Time hora = jogo.getHora();
        if (data == null || hora == null) {
            return null;
        }
        LocalDate localDate = Instant.ofEpochMilli(data.getTime()).atZone(zoneId).toLocalDate();
        LocalTime localTime = hora.toLocalTime();
        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);
        return ZonedDateTime.of(dateTime, zoneId);
    }
}
