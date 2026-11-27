package com.energymix.reservation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @Operation(summary = "Book a charger for 1-6 full hours")
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestHeader("X-Client-Id") UUID clientId,
                                      @Valid @RequestBody CreateReservationRequest request) {
        return service.create(clientId, request);
    }

    @Operation(summary = "List my upcoming reservations")
    @GetMapping("/reservations")
    public List<ReservationResponse> getMine(@RequestHeader("X-Client-Id") UUID clientId) {
        return service.findUpcoming(clientId);
    }

    @Operation(summary = "Cancel my reservation")
    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@RequestHeader("X-Client-Id") UUID clientId, @PathVariable Long id) {
        service.cancel(clientId, id);
    }

    @Operation(summary = "Occupied time slots of a charger on a given UK day")
    @GetMapping("/chargers/{chargerId}/reservations")
    public List<OccupiedSlot> getOccupied(@PathVariable Long chargerId, @RequestParam LocalDate date,
                                          @RequestHeader("X-Client-Id") UUID clientId) {
        return service.findOccupied(chargerId, date, clientId);
    }
}
