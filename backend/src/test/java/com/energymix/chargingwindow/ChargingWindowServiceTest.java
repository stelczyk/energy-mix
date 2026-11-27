package com.energymix.chargingwindow;

import com.energymix.carbonintensity.CarbonIntensityClient;
import com.energymix.carbonintensity.FuelShare;
import com.energymix.carbonintensity.FuelType;
import com.energymix.carbonintensity.GenerationInterval;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChargingWindowServiceTest {

    // summer date on purpose: London = UTC+1, so "tomorrow" starts at 23:00 UTC
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneId.of("Europe/London"));
    private final CarbonIntensityClient client = mock(CarbonIntensityClient.class);
    private final ChargingWindowService service = new ChargingWindowService(client, clock);

    @Test
    void shouldPickCleanestWindow_evenWhenItCrossesMidnight() {
        // given: 23:00, 23:30, 00:00, 00:30 London time
        when(client.getGeneration(any(), any())).thenReturn(List.of(
                interval("2026-07-02T22:00Z", 10),
                interval("2026-07-02T22:30Z", 80),
                interval("2026-07-02T23:00Z", 90),
                interval("2026-07-02T23:30Z", 20)));

        // when
        ChargingWindow result = service.findCleanestWindow(1);

        // then: 23:30 (2 July) -> 00:30 (3 July) London, avg (80 + 90) / 2
        assertThat(result).isEqualTo(new ChargingWindow(
                OffsetDateTime.parse("2026-07-02T23:30+01:00"),
                OffsetDateTime.parse("2026-07-03T00:30+01:00"),
                85.0));
        verify(client).getGeneration(Instant.parse("2026-07-01T23:00:00Z"), Instant.parse("2026-07-03T23:00:00Z"));
    }

    @Test
    void shouldSkipWindowsWithMissingSlots() {
        // given: 10:30 slot is missing, so 10:00 + 11:00 is NOT a 1-hour window
        when(client.getGeneration(any(), any())).thenReturn(List.of(
                interval("2026-07-02T10:00Z", 90),
                interval("2026-07-02T11:00Z", 90),
                interval("2026-07-02T11:30Z", 40)));

        // when
        ChargingWindow result = service.findCleanestWindow(1);

        // then: the gap window (avg 90) is skipped
        assertThat(result.start()).isEqualTo(OffsetDateTime.parse("2026-07-02T12:00+01:00"));
        assertThat(result.cleanPercentage()).isEqualTo(65.0);
    }

    @Test
    void shouldThrow_whenNoContinuousWindowExists() {
        // given
        when(client.getGeneration(any(), any())).thenReturn(List.of(interval("2026-07-02T10:00Z", 50)));

        // when & then
        assertThatThrownBy(() -> service.findCleanestWindow(1))
                .isInstanceOf(NotEnoughForecastDataException.class);
    }

    // WIND = clean share, GAS = the rest
    private static GenerationInterval interval(String from, double clean) {
        OffsetDateTime start = OffsetDateTime.parse(from);
        return new GenerationInterval(start, start.plusMinutes(30),
                List.of(new FuelShare(FuelType.WIND, clean), new FuelShare(FuelType.GAS, 100 - clean)));
    }
}
