package com.energymix.energymix;

import com.energymix.carbonintensity.CarbonIntensityClient;
import com.energymix.carbonintensity.FuelShare;
import com.energymix.carbonintensity.FuelType;
import com.energymix.carbonintensity.GenerationInterval;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

@Service
public class EnergyMixService {

    private static final int DAYS = 3; // today, tomorrow, day after

    private final CarbonIntensityClient client;
    private final Clock clock;

    public EnergyMixService(CarbonIntensityClient client, Clock clock) {
        this.client = client;
        this.clock = clock;
    }

    public List<DailyMix> getDailyMix() {
        ZoneId zone = clock.getZone();
        LocalDate today = LocalDate.now(clock);
        Instant from = today.atStartOfDay(zone).toInstant();
        Instant to = today.plusDays(DAYS).atStartOfDay(zone).toInstant();

        Map<LocalDate, List<GenerationInterval>> byDay = client.getGeneration(from, to).stream()
                // API also returns the interval ending at `from` (previous day)
                .filter(interval -> !interval.from().toInstant().isBefore(from))
                .collect(groupingBy(interval -> interval.from().atZoneSameInstant(zone).toLocalDate()));

        // always all 3 days - a day without forecast is returned empty instead of disappearing
        return today.datesUntil(today.plusDays(DAYS))
                .map(date -> toDailyMix(date, byDay.getOrDefault(date, List.of()), zone))
                .toList();
    }

    private DailyMix toDailyMix(LocalDate date, List<GenerationInterval> intervals, ZoneId zone) {
        if (intervals.isEmpty()) {
            return new DailyMix(date, Map.of(), null, false);
        }

        Map<FuelType, Double> averages = intervals.stream()
                .flatMap(interval -> interval.generationMix().stream())
                .collect(groupingBy(FuelShare::fuel, () -> new EnumMap<>(FuelType.class),
                        averagingDouble(FuelShare::perc)));

        double clean = averages.entrySet().stream()
                .filter(entry -> entry.getKey().isClean())
                .mapToDouble(Map.Entry::getValue)
                .sum();

        averages.replaceAll((fuel, value) -> roundToOneDecimal(value));
        return new DailyMix(date, averages, roundToOneDecimal(clean),
                intervals.size() == expectedIntervals(date, zone));
    }

    // 48 normally, 46/50 on DST change days
    private static long expectedIntervals(LocalDate date, ZoneId zone) {
        return Duration.between(date.atStartOfDay(zone), date.plusDays(1).atStartOfDay(zone)).toMinutes() / 30;
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
