package com.energymix.carbonintensity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(CarbonIntensityClient.class)
class CarbonIntensityClientTest {

    @Autowired
    CarbonIntensityClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void shouldParseGenerationData_whenApiReturns200() {
        // given (real API format: timestamps without seconds)
        server.expect(requestTo("https://api.carbonintensity.org.uk/generation/2026-11-27T00:00:00Z/2026-11-27T00:30:00Z"))
                .andRespond(withSuccess("""
                        {"data":[{"from":"2026-11-27T00:00Z","to":"2026-11-27T00:30Z",
                          "generationmix":[{"fuel":"wind","perc":55.8},{"fuel":"gas","perc":24.1},{"fuel":"tidal","perc":0.1}]}]}
                        """, MediaType.APPLICATION_JSON));

        // when
        List<GenerationInterval> result = client.getGeneration(
                Instant.parse("2026-11-27T00:00:00Z"), Instant.parse("2026-11-27T00:30:00Z"));

        // then
        assertThat(result).containsExactly(new GenerationInterval(
                OffsetDateTime.parse("2026-11-27T00:00Z"),
                OffsetDateTime.parse("2026-11-27T00:30Z"),
                List.of(new FuelShare(FuelType.WIND, 55.8),
                        new FuelShare(FuelType.GAS, 24.1),
                        new FuelShare(FuelType.UNKNOWN, 0.1))));
    }

    @Test
    void shouldThrowRestClientException_whenApiReturns500() {
        // given
        server.expect(requestTo(containsString("/generation")))
                .andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> client.getGeneration(Instant.EPOCH, Instant.EPOCH))
                .isInstanceOf(RestClientException.class);
    }
}
