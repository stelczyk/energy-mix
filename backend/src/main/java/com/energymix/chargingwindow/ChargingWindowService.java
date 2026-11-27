package com.energymix.chargingwindow;

import com.energymix.carbonintensity.CarbonIntensityClient;
import com.energymix.carbonintensity.GenerationInterval;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ChargingWindowService {

    private static final int FORECAST_DAYS = 2; // tomorrow and the day after

    private final CarbonIntensityClient client;
    private final Clock clock;

    public ChargingWindowService(CarbonIntensityClient client, Clock clock) {
        this.client = client;
        this.clock = clock;
    }

    public ChargingWindow findCleanestWindow(int hours) {
        ZoneId zone = clock.getZone();
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        Instant from = tomorrow.atStartOfDay(zone).toInstant();
        Instant to = tomorrow.plusDays(FORECAST_DAYS).atStartOfDay(zone).toInstant();

        List<GenerationInterval> intervals = client.getGeneration(from, to).stream()
                // API also returns the interval ending at `from` (previous day)
                .filter(interval -> !interval.from().toInstant().isBefore(from))
                .toList();

        int slots = hours * 2; // data comes in 30-minute intervals
        ChargingWindow best = null;

        for (int i = 0; i + slots <= intervals.size(); i++) {
            List<GenerationInterval> window = intervals.subList(i, i + slots);
            OffsetDateTime start = window.getFirst().from();
            OffsetDateTime end = window.getLast().to();

            // missing slots in the data would stretch the window beyond the requested hours - skip it
            if (!end.isEqual(start.plusHours(hours))) {
                continue;
            }

            double clean = window.stream()
                    .mapToDouble(GenerationInterval::cleanPercentage)
                    .average()
                    .orElseThrow();

            if (best == null || clean > best.cleanPercentage()) {
                best = new ChargingWindow(
                        start.atZoneSameInstant(zone).toOffsetDateTime(),
                        end.atZoneSameInstant(zone).toOffsetDateTime(),
                        clean);
            }
        }

        if (best == null) {
            throw new NotEnoughForecastDataException(hours);
        }
        // round only at the end - comparing windows must use full precision
        return new ChargingWindow(best.start(), best.end(), Math.round(best.cleanPercentage() * 10) / 10.0);
    }
}
