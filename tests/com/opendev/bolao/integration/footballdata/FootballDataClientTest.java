package com.opendev.bolao.integration.footballdata;

import com.opendev.bolao.integration.footballdata.dto.MatchDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FootballDataClientTest {

    @Test
    void shouldInitializeWithRateLimit() {
        FootballDataClient client = new FootballDataClient("mock-token");
        assertThat(client.getRequestsRemaining()).isEqualTo(10);
    }
    
    // Nota: Testes de integração real exigiriam MockWebServer para simular respostas da API.
    // Como diretriz, priorizaremos testes unitários de lógica de negócio e mapeamento.
}
