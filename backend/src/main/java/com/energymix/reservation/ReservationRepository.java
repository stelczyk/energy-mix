package com.energymix.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = "charger")
    List<Reservation> findByClientIdAndEndTimeAfterOrderByStartTime(UUID clientId, Instant now);

    Optional<Reservation> findByIdAndClientId(Long id, UUID clientId);
}
