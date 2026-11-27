package com.energymix.chargingwindow;

import java.time.OffsetDateTime;

public record ChargingWindow(OffsetDateTime start, OffsetDateTime end, double cleanPercentage) {
}
