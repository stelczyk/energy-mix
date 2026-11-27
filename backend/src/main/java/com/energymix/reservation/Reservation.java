package com.energymix.reservation;

import com.energymix.charger.Charger;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Charger charger;

    private UUID clientId;

    private Instant startTime;

    private Instant endTime;

    protected Reservation() {
    }

    public Reservation(Charger charger, UUID clientId, Instant startTime, Instant endTime) {
        this.charger = charger;
        this.clientId = clientId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Charger getCharger() {
        return charger;
    }

    public UUID getClientId() {
        return clientId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }
}
