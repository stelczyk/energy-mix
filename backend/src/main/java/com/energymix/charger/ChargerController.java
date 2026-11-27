package com.energymix.charger;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chargers")
public class ChargerController {

    private final ChargerRepository repository;

    public ChargerController(ChargerRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "List all chargers")
    @GetMapping
    public List<ChargerResponse> getChargers() {
        return repository.findAll(Sort.by("id")).stream()
                .map(ChargerResponse::from)
                .toList();
    }
}
