package com.energymix.carbonintensity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

// OffsetDateTime, not Instant: API sends "2026-11-27T00:00Z" (no seconds), which Instant can't parse
public record GenerationInterval(OffsetDateTime from, OffsetDateTime to,
                                 @JsonProperty("generationmix") List<FuelShare> generationMix) {

    public double cleanPercentage() {
        return generationMix.stream()
                .filter(share -> share.fuel().isClean())
                .mapToDouble(FuelShare::perc)
                .sum();
    }
}
