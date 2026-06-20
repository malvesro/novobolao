package com.opendev.bolao.integration.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreDto {
    @JsonProperty("winner")
    private String winner;
    @JsonProperty("duration")
    private String duration;
    @JsonProperty("fullTime")
    private ScorePairDto fullTime;
    @JsonProperty("halfTime")
    private ScorePairDto halfTime;
    @JsonProperty("extraTime")
    private ScorePairDto extraTime;
    @JsonProperty("penalties")
    private ScorePairDto penalties;

    public String getWinner() { return winner; }
    public String getDuration() { return duration; }
    public ScorePairDto getFullTime() { return fullTime; }
    public ScorePairDto getExtraTime() { return extraTime; }
    public ScorePairDto getPenalties() { return penalties; }
}
