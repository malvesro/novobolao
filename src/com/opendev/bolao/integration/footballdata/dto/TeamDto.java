package com.opendev.bolao.integration.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
}
