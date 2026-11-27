package com.energymix.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull Long chargerId,
        @NotNull LocalDateTime startTime,
        @Min(1) @Max(6) int hours) {
}
