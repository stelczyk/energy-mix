package com.energymix.carbonintensity;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum FuelType {
    BIOMASS(true), NUCLEAR(true), HYDRO(true), WIND(true), SOLAR(true),
    GAS(false), COAL(false), IMPORTS(false), OTHER(false),
    UNKNOWN(false); // fallback when the API adds a fuel we don't know yet

    private final boolean clean;

    FuelType(boolean clean) {
        this.clean = clean;
    }

    public boolean isClean() {
        return clean;
    }

    @JsonCreator
    public static FuelType fromApi(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
