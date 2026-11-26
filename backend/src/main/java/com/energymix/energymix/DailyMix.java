package com.energymix.energymix;

import com.energymix.carbonintensity.FuelType;

import java.time.LocalDate;
import java.util.Map;

public record DailyMix(LocalDate date, Map<FuelType, Double> fuels, double cleanPercentage) {
}
