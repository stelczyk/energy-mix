package com.energymix.energymix;

import com.energymix.carbonintensity.CarbonIntensityClient;
import com.energymix.carbonintensity.FuelShare;
import com.energymix.carbonintensity.FuelType;
import com.energymix.carbonintensity.GenerationInterval;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
                .collect(groupingBy(interval -> interval.from().atZoneSameInstant(zone).toLocalDate(),
                        TreeMap::new, toList()));

        return byDay.entrySet().stream()
                .map(day -> toDailyMix(day.getKey(), day.getValue()))
                .toList();
    }

    private DailyMix toDailyMix(LocalDate date, List<GenerationInterval> intervals) {
        Map<FuelType, Double> averages = intervals.stream()
                .flatMap(interval -> interval.generationMix().stream())
                .collect(groupingBy(FuelShare::fuel, () -> new EnumMap<>(FuelType.class),
                        averagingDouble(FuelShare::perc)));

        double clean = averages.entrySet().stream()
                .filter(entry -> entry.getKey().isClean())
                .mapToDouble(Map.Entry::getValue)
                .sum();

        averages.replaceAll((fuel, value) -> roundToOneDecimal(value));
        return new DailyMix(date, averages, roundToOneDecimal(clean));
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
