package com.energymix.reservation;

import java.time.Instant;

public record ReservationResponse(Long id, Long chargerId, String chargerName,
                                  Instant startTime, Instant endTime) {

    static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getCharger().getId(),
                reservation.getCharger().getName(), reservation.getStartTime(), reservation.getEndTime());
    }
}
