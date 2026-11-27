package com.energymix.reservation;

import java.time.Instant;

public record OccupiedSlot(Instant startTime, Instant endTime, boolean mine) {
}
