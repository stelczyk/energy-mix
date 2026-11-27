package com.energymix.energymix;

import com.energymix.carbonintensity.FuelType;

import java.time.LocalDate;
import java.util.Map;

// cleanPercentage is null when there is no forecast for the day yet;
// complete is false when some half-hour intervals are missing (forecast reaches only ~1.5 days ahead)
public record DailyMix(LocalDate date, Map<FuelType, Double> fuels, Double cleanPercentage, boolean complete) {
}
