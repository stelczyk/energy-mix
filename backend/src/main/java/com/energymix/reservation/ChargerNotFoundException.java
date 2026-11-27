package com.energymix.reservation;

public class ChargerNotFoundException extends RuntimeException {

    public ChargerNotFoundException(Long chargerId) {
        super("Charger " + chargerId + " not found.");
    }
}
