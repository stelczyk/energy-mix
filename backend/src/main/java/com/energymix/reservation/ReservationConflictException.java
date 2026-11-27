package com.energymix.reservation;

public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException() {
        super("This charger is already booked for the selected time.");
    }
}
