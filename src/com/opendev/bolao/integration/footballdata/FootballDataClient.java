package com.opendev.bolao.integration.footballdata;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opendev.bolao.integration.footballdata.dto.MatchDto;
import com.opendev.bolao.integration.footballdata.dto.MatchResponseDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class FootballDataClient {

    private static final Logger LOG = Logger.getLogger(FootballDataClient.class.getName());
    private static final String BASE_URL = "https://api.football-data.org/v4";
    private static final String COMPETITION = "WC";
    private static final String HEADER_TOKEN = "X-Auth-Token";
    private static final String HEADER_REMAINING = "X-Requests-Available-Minute";

    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestsRemaining = new AtomicInteger(10);

    public FootballDataClient(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<MatchDto> fetchLiveMatches() throws IOException, InterruptedException {
        String url = BASE_URL + "/competitions/" + COMPETITION + "/matches?status=IN_PLAY";
        return fetchMatches(url);
    }

    public List<MatchDto> fetchMatchesByDate(Instant from, Instant to) throws IOException, InterruptedException {
        String dateFrom = from.toString().substring(0, 10);
        String dateTo = to.toString().substring(0, 10);
        String url = BASE_URL + "/competitions/" + COMPETITION + "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo;
        return fetchMatches(url);
    }

    public MatchDto fetchMatchById(Long matchId) throws IOException, InterruptedException {
        String url = BASE_URL + "/matches/" + matchId;
        HttpRequest request = createRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        updateRateLimit(response);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), MatchDto.class);
        } else {
            LOG.warning("Failed to fetch match " + matchId + ". Status: " + response.statusCode());
            return null;
        }
    }

    private List<MatchDto> fetchMatches(String url) throws IOException, InterruptedException {
        if (requestsRemaining.get() <= 0) {
            LOG.warning("Rate limit reached. Skipping request to: " + url);
            return List.of();
        }

        HttpRequest request = createRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        updateRateLimit(response);

        if (response.statusCode() == 200) {
            MatchResponseDto dto = objectMapper.readValue(response.body(), MatchResponseDto.class);
            return dto.getMatches();
        } else if (response.statusCode() == 429) {
            LOG.severe("HTTP 429: Rate Limit Exceeded");
            requestsRemaining.set(0);
        } else {
            LOG.warning("API Error: " + response.statusCode() + " - " + response.body());
        }
        return List.of();
    }

    private HttpRequest createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_TOKEN, apiToken)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private void updateRateLimit(HttpResponse<?> response) {
        response.headers().firstValue(HEADER_REMAINING).ifPresent(val -> {
            try {
                requestsRemaining.set(Integer.parseInt(val));
            } catch (NumberFormatException e) {
                // ignore
            }
        });
    }

    public int getRequestsRemaining() {
        return requestsRemaining.get();
    }
}
