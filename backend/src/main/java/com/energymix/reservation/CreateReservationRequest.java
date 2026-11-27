package com.energymix.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateReservationRequest(
        @NotNull Long chargerId,
        @NotNull OffsetDateTime startTime,
        @Min(1) @Max(6) int hours) {
}
