package com.opendev.bolao.integration.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResponseDto {
    @JsonProperty("matches")
    private List<MatchDto> matches;

    public List<MatchDto> getMatches() { return matches; }
}
