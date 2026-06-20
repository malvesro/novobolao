package com.opendev.bolao.integration.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScorePairDto {
    @JsonProperty("home")
    private Integer home;
    @JsonProperty("away")
    private Integer away;

    public int getHome() { return home == null ? 0 : home; }
    public int getAway() { return away == null ? 0 : away; }
}
