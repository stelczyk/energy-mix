package com.energymix.energymix;

import com.energymix.carbonintensity.CarbonIntensityClient;
import com.energymix.carbonintensity.FuelShare;
import com.energymix.carbonintensity.FuelType;
import com.energymix.carbonintensity.GenerationInterval;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnergyMixServiceTest {

    // summer date on purpose: London = UTC+1, so day boundaries differ from UTC
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneId.of("Europe/London"));
    private final CarbonIntensityClient client = mock(CarbonIntensityClient.class);
    private final EnergyMixService service = new EnergyMixService(client, clock);

    @Test
    void shouldGroupByLondonDayAndAverageFuels_whenApiReturnsData() {
        // given
        when(client.getGeneration(any(), any())).thenReturn(List.of(
                interval("2026-06-30T22:30Z", 100, 0),  // 23:30 London, 30 June -> dropped
                interval("2026-06-30T23:00Z", 60, 40),  // 00:00 London, 1 July
                interval("2026-07-01T12:00Z", 80, 20),  // 1 July
                interval("2026-07-01T23:00Z", 10, 90)   // 00:00 London, 2 July
        ));

        // when
        List<DailyMix> result = service.getDailyMix();

        // then
        assertThat(result).extracting(DailyMix::date)
                .containsExactly(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-02"));
        assertThat(result.get(0).fuels()).containsEntry(FuelType.WIND, 70.0).containsEntry(FuelType.GAS, 30.0);
        assertThat(result.get(0).cleanPercentage()).isEqualTo(70.0);
        verify(client).getGeneration(Instant.parse("2026-06-30T23:00:00Z"), Instant.parse("2026-07-03T23:00:00Z"));
    }

    private static GenerationInterval interval(String from, double wind, double gas) {
        OffsetDateTime start = OffsetDateTime.parse(from);
        return new GenerationInterval(start, start.plusMinutes(30),
                List.of(new FuelShare(FuelType.WIND, wind), new FuelShare(FuelType.GAS, gas)));
    }
}
