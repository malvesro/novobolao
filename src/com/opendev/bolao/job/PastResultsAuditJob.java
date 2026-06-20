package com.opendev.bolao.job;

import com.opendev.bolao.integration.footballdata.FootballDataClient;
import com.opendev.bolao.integration.footballdata.dto.MatchDto;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.util.BolaoTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Job de Auditoria Diária.
 * Verifica todos os jogos do passado (desde o início do torneio) para garantir
 * que não haja divergências entre o nosso banco e a API oficial.
 */
public class PastResultsAuditJob {

    private static final Log logger = LogFactory.getLog(PastResultsAuditJob.class);

    private JogoService jogoService;
    private String apiToken;
    private boolean enabled;

    public void execute() {
        if (!enabled || apiToken == null || apiToken.isEmpty()) {
            return;
        }

        FootballDataClient client = new FootballDataClient(apiToken);
        try {
            logger.info("[PastResultsAuditJob] Iniciando auditoria de resultados passados...");
            
            ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, BolaoTime.getZoneId());
            ZonedDateTime end = ZonedDateTime.of(2026, 7, 20, 23, 59, 59, 0, BolaoTime.getZoneId());
            Instant from = start.toInstant();
            Instant to = end.toInstant();
            
            List<MatchDto> matches = client.fetchMatchesByDate(from, to);
            int correcoes = 0;

            for (MatchDto dto : matches) {
                if ("FINISHED".equalsIgnoreCase(dto.getStatus())) {
                    correcoes += verificarECorrigir(dto);
                }
            }
            
            logger.info("[PastResultsAuditJob] Auditoria concluída. Correções realizadas: " + correcoes);
        } catch (Exception e) {
            logger.error("[PastResultsAuditJob] Houve um erro durante a auditoria!", e);
        }
    }

    private int verificarECorrigir(MatchDto dto) {
        String externalId = String.valueOf(dto.getId());
        Optional<Jogo> jogoOpt = jogoService.buscarPorIdExterno(externalId);
        
        if (jogoOpt.isPresent()) {
            Jogo jogo = jogoOpt.get();
            Integer apiGols1 = dto.getScore().getFullTime().getHome();
            Integer apiGols2 = dto.getScore().getFullTime().getAway();

            if (apiGols1 == null || apiGols2 == null) return 0;

            // Chave da Auditoria: Só atualiza se houver divergência ou se o nosso banco estiver zerado/nulo
            boolean divergente = !apiGols1.equals(jogo.getGolsEquipe1()) || !apiGols2.equals(jogo.getGolsEquipe2());
            
            if (divergente) {
                logger.warn("[PastResultsAuditJob] DISCREPÂNCIA DETECTADA no Jogo " + jogo.getId() + 
                            " (" + jogo.getRepresentacaoEquipes() + "). " +
                            "Banco: " + jogo.getGolsEquipe1() + "x" + jogo.getGolsEquipe2() + 
                            " | API: " + apiGols1 + "x" + apiGols2 + ". Corrigindo...");
                
                jogoService.processarAtualizacaoScore(externalId, apiGols1, apiGols2, null);
                return 1;
            }
        }
        return 0;
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
