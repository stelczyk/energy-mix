package com.energymix.reservation;

import com.energymix.charger.Charger;
import com.energymix.charger.ChargerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChargerRepository chargerRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, ChargerRepository chargerRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.chargerRepository = chargerRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(UUID clientId, CreateReservationRequest request) {
        Instant start = request.startTime().toInstant();
        if (start.isBefore(clock.instant())) {
            throw new InvalidReservationException("Reservation cannot start in the past.");
        }
        if (start.getEpochSecond() % 1800 != 0 || start.getNano() != 0) {
            throw new InvalidReservationException("Reservation must start on a full or half hour.");
        }
        Charger charger = chargerRepository.findById(request.chargerId())
                .orElseThrow(() -> new ChargerNotFoundException(request.chargerId()));

        Reservation reservation = new Reservation(charger, clientId, start,
                start.plus(request.hours(), ChronoUnit.HOURS));
        try {
            return ReservationResponse.from(reservationRepository.saveAndFlush(reservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationConflictException();
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findUpcoming(UUID clientId) {
        return reservationRepository.findByClientIdAndEndTimeAfterOrderByStartTime(clientId, clock.instant())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void cancel(UUID clientId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndClientId(reservationId, clientId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        reservationRepository.delete(reservation);
    }
}
