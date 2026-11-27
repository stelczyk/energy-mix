package com.energymix.charger;

public record ChargerResponse(Long id, String name, String location) {

    static ChargerResponse from(Charger charger) {
        return new ChargerResponse(charger.getId(), charger.getName(), charger.getLocation());
    }
}
