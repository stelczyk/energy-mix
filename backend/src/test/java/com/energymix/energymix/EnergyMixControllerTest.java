package com.energymix.energymix;

import com.energymix.carbonintensity.FuelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(EnergyMixController.class)
class EnergyMixControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    EnergyMixService service;

    @Test
    void shouldReturnDailyMix_whenServiceReturnsData() {
        // given
        when(service.getDailyMix()).thenReturn(List.of(
                new DailyMix(LocalDate.parse("2026-11-27"), Map.of(FuelType.WIND, 60.0), 60.0)));

        // when & then
        assertThat(mvc.get().uri("/api/v1/energy/mix"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        [{"date":"2026-11-27","fuels":{"WIND":60.0},"cleanPercentage":60.0}]
                        """);
    }

    @Test
    void shouldReturn503_whenExternalApiFails() {
        // given
        when(service.getDailyMix()).thenThrow(new ResourceAccessException("timeout"));

        // when & then
        assertThat(mvc.get().uri("/api/v1/energy/mix"))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
