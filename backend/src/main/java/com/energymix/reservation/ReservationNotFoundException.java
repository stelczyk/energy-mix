package com.energymix.reservation;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long id) {
        super("Reservation " + id + " not found.");
    }
}
