package com.opendev.bolao.job;

import com.opendev.bolao.integration.footballdata.FootballDataClient;
import com.opendev.bolao.integration.footballdata.dto.MatchDto;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.JogoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.ConversaoUtils;

import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.util.FlagUtils;

public class FixtureRefreshJob {

    private static final Log logger = LogFactory.getLog(FixtureRefreshJob.class);

    private JogoService jogoService;
    private EquipeService equipeService;
    private String apiToken;
    private boolean enabled;

    public void execute() {
        if (!enabled) return;

        FootballDataClient client = new FootballDataClient(apiToken);
        try {
            logger.info("[FixtureRefreshJob] Sincronizando agenda e realizando auto-vínculo de IDs...");
            ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, BolaoTime.getZoneId());
            ZonedDateTime end = ZonedDateTime.of(2026, 7, 20, 23, 59, 59, 0, BolaoTime.getZoneId());
            Instant from = start.toInstant();
            Instant to = end.toInstant();
            
            List<MatchDto> matches = client.fetchMatchesByDate(from, to);
            
            for (MatchDto dto : matches) {
                vinculoPorExternalIdOuTimes(dto);
            }
            
            logger.info("[FixtureRefreshJob] Sincronização concluída.");
        } catch (Exception e) {
            logger.error("[FixtureRefreshJob] Erro ao sincronizar agenda!", e);
        }
    }

    private void vinculoPorExternalIdOuTimes(MatchDto dto) {
        String externalId = String.valueOf(dto.getId());
        Optional<Jogo> jogoOpt = jogoService.buscarPorIdExterno(externalId);
        
        ZonedDateTime kickoffBrt = dto.getKickoffUtc().atZone(BolaoTime.getZoneId());
        java.util.Date dataDaApi = java.util.Date.from(kickoffBrt.toLocalDate().atStartOfDay(BolaoTime.getZoneId()).toInstant());
        java.sql.Time horaDaApi = java.sql.Time.valueOf(kickoffBrt.toLocalTime());

        if (jogoOpt.isEmpty()) {
            // Tenta Auto-Vínculo por Data + Times
            if (dto.getHomeTeam() != null && dto.getAwayTeam() != null) {
                Optional<Equipe> eq1 = buscarEquipeLocalPelosNomes(dto.getHomeTeam().getName());
                Optional<Equipe> eq2 = buscarEquipeLocalPelosNomes(dto.getAwayTeam().getName());
                
                logger.info("[DEBUG] Tradução para " + dto.getHomeTeam().getName() + " vs " + dto.getAwayTeam().getName() + 
                            ": Eq1=" + (eq1.isPresent() ? eq1.get().getNomePais() : "NULL") + 
                            ", Eq2=" + (eq2.isPresent() ? eq2.get().getNomePais() : "NULL"));

                if (eq1.isPresent() && eq2.isPresent()) {
                    jogoOpt = jogoService.buscarPorDataETimes(dataDaApi, eq1.get().getId(), eq2.get().getId());
                    if (jogoOpt.isPresent()) {
                        Jogo jogoEncontrado = jogoOpt.get();
                        logger.info("[FixtureRefreshJob] Auto-vínculo detectado para jogo " + jogoEncontrado.getId() + 
                                    " (" + eq1.get().getNomePais() + " x " + eq2.get().getNomePais() + "). Vinculando ID: " + externalId);
                        jogoEncontrado.setExternalId(externalId);
                        // Força atualização para gravar o ID
                        jogoService.atualizarDadosEstruturaisJogo(
                            jogoEncontrado.getId(), jogoEncontrado.getData(), jogoEncontrado.getHora(), 
                            jogoEncontrado.getLocal(), jogoEncontrado.getFase(), 
                            jogoEncontrado.getEquipe1().getId(), jogoEncontrado.getEquipe2().getId(),
                            externalId
                        );

                        // Atualiza placar imediatamente se o jogo já acabou
                        if ("FINISHED".equalsIgnoreCase(dto.getStatus()) && dto.getScore() != null && dto.getScore().getFullTime() != null) {
                            Integer g1 = dto.getScore().getFullTime().getHome();
                            Integer g2 = dto.getScore().getFullTime().getAway();
                            if (g1 != null && g2 != null) {
                                logger.info("[FixtureRefreshJob] Aplicando placar imediato após vínculo: " + g1 + " x " + g2);
                                jogoService.processarAtualizacaoScore(externalId, g1, g2, null);
                            }
                        }
                    }
                }
            }
        }

        if (jogoOpt.isPresent()) {
            Jogo jogo = jogoOpt.get();
            boolean mudouEstrutura = false;

            // 1. Sincronização de Agenda
            if (!jogo.jaOcorreu() && (!dataDaApi.equals(jogo.getData()) || !horaDaApi.equals(jogo.getHora()))) {
                logger.info("[FixtureRefreshJob] Ajustando horário: Jogo " + jogo.getId() + " (" + externalId + ")");
                mudouEstrutura = true;
            }

            // 2. Sincronização de Times (Mata-mata / Promotion)
            Long idEq1 = jogo.getEquipe1().getId();
            Long idEq2 = jogo.getEquipe2().getId();

            if (dto.getHomeTeam() != null && dto.getHomeTeam().getName() != null) {
                Optional<Equipe> eq1Api = buscarEquipeLocalPelosNomes(dto.getHomeTeam().getName());
                if (eq1Api.isPresent() && !eq1Api.get().getId().equals(idEq1)) {
                    logger.info("[FixtureRefreshJob] Promovendo Equipe 1: " + eq1Api.get().getNomePais() + " no jogo " + jogo.getId());
                    idEq1 = eq1Api.get().getId();
                    mudouEstrutura = true;
                }
            }

            if (dto.getAwayTeam() != null && dto.getAwayTeam().getName() != null) {
                Optional<Equipe> eq2Api = buscarEquipeLocalPelosNomes(dto.getAwayTeam().getName());
                if (eq2Api.isPresent() && !eq2Api.get().getId().equals(idEq2)) {
                    logger.info("[FixtureRefreshJob] Promovendo Equipe 2: " + eq2Api.get().getNomePais() + " no jogo " + jogo.getId());
                    idEq2 = eq2Api.get().getId();
                    mudouEstrutura = true;
                }
            }

            if (mudouEstrutura) {
                jogoService.atualizarDadosEstruturaisJogo(
                    jogo.getId(), dataDaApi, horaDaApi, jogo.getLocal(), 
                    jogo.getFase(), idEq1, idEq2, externalId
                );
            }
        }
    }

    private Optional<Equipe> buscarEquipeLocalPelosNomes(String nomeApi) {
        String countryCode = FlagUtils.countryCodeFromName(nomeApi);
        if (countryCode == null || countryCode.isEmpty()) return Optional.empty();
        
        return equipeService.buscarApenasPaisesReais().stream()
                .filter(e -> countryCode.equalsIgnoreCase(e.getCodigoPais()))
                .findFirst();
    }

    public void setEquipeService(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    public void setJogoService(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
