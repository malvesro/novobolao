package com.opendev.bolao.integration.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("utcDate")
    private Instant kickoffUtc;
    @JsonProperty("status")
    private String status;
    @JsonProperty("stage")
    private String stage;
    @JsonProperty("homeTeam")
    private TeamDto homeTeam;
    @JsonProperty("awayTeam")
    private TeamDto awayTeam;
    @JsonProperty("score")
    private ScoreDto score;

    public Long getId() { return id; }
    public Instant getKickoffUtc() { return kickoffUtc; }
    public String getStatus() { return status; }
    public String getStage() { return stage; }
    public TeamDto getHomeTeam() { return homeTeam; }
    public TeamDto getAwayTeam() { return awayTeam; }
    public ScoreDto getScore() { return score; }
}
