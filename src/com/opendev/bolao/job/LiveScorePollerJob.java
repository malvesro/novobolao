package com.opendev.bolao.job;

import com.opendev.bolao.integration.footballdata.FootballDataClient;
import com.opendev.bolao.integration.footballdata.dto.MatchDto;
import com.opendev.bolao.service.JogoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class LiveScorePollerJob {

    private static final Log logger = LogFactory.getLog(LiveScorePollerJob.class);

    private JogoService jogoService;
    private String apiToken;
    private boolean enabled;

    public void execute() {
        if (!enabled) {
            return;
        }

        if (apiToken == null || apiToken.isBlank()) {
            logger.warn("[LiveScorePollerJob] API Token não configurado. Pulando execução.");
            return;
        }

        FootballDataClient client = new FootballDataClient(apiToken);
        
        try {
            logger.info("[LiveScorePollerJob] Iniciando polling de jogos em andamento...");
            List<MatchDto> liveMatches = client.fetchLiveMatches();
            
            if (liveMatches.isEmpty()) {
                logger.info("[LiveScorePollerJob] Nenhum jogo em andamento encontrado na API.");
                return;
            }

            for (MatchDto match : liveMatches) {
                if (match.getScore() != null && match.getScore().getFullTime() != null) {
                    jogoService.processarAtualizacaoScore(
                        String.valueOf(match.getId()),
                        match.getScore().getFullTime().getHome(),
                        match.getScore().getFullTime().getAway(),
                        match.getKickoffUtc() // Simplified source date for now
                    );
                }
            }
            
            logger.info("[LiveScorePollerJob] Ciclo de atualização concluído. Rate limit restante: " + client.getRequestsRemaining());
            
        } catch (Exception e) {
            logger.error("[LiveScorePollerJob] Erro durante o polling de scores!", e);
        }
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
