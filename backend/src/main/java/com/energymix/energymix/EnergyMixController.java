package com.energymix.energymix;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/energy")
public class EnergyMixController {

    private final EnergyMixService service;

    public EnergyMixController(EnergyMixService service) {
        this.service = service;
    }

    @Operation(summary = "Average generation mix and clean energy share for today, tomorrow and the day after (Europe/London days)")
    @GetMapping("/mix")
    public List<DailyMix> getDailyMix() {
        return service.getDailyMix();
    }
}
