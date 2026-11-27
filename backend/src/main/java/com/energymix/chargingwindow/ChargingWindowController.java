package com.energymix.chargingwindow;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/energy")
public class ChargingWindowController {

    private final ChargingWindowService service;

    public ChargingWindowController(ChargingWindowService service) {
        this.service = service;
    }

    @Operation(summary = "Cleanest charging window of the given length within tomorrow and the day after (Europe/London)")
    @GetMapping("/charging-window")
    public ChargingWindow findCleanestWindow(@RequestParam @Min(1) @Max(6) int hours) {
        return service.findCleanestWindow(hours);
    }
}
