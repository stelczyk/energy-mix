package com.energymix.reservation;

import com.energymix.charger.Charger;
import com.energymix.charger.ChargerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private static final UUID CLIENT_ID = UUID.randomUUID();

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneId.of("Europe/London"));
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ChargerRepository chargerRepository = mock(ChargerRepository.class);
    private final ReservationService service = new ReservationService(reservationRepository, chargerRepository, clock);

    @Test
    void shouldCreateReservation_whenRequestIsValid() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReservationResponse result = service.create(CLIENT_ID, request("2026-07-01T14:00:00Z", 3));

        // then
        assertThat(result.startTime()).isEqualTo(Instant.parse("2026-07-01T14:00:00Z"));
        assertThat(result.endTime()).isEqualTo(Instant.parse("2026-07-01T17:00:00Z"));
    }

    @Test
    void shouldReject_whenStartIsInThePast() {
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T09:00:00Z", 1)))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("past");
    }

    @Test
    void shouldAllowStart_onHalfHour() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReservationResponse result = service.create(CLIENT_ID, request("2026-07-01T14:30:00Z", 2));

        // then
        assertThat(result.endTime()).isEqualTo(Instant.parse("2026-07-01T16:30:00Z"));
    }

    @Test
    void shouldReject_whenStartIsNotOnFullOrHalfHour() {
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:15:00Z", 1)))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("full or half hour");
    }

    @Test
    void shouldReject_whenChargerDoesNotExist() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:00:00Z", 1)))
                .isInstanceOf(ChargerNotFoundException.class);
    }

    @Test
    void shouldThrowConflict_whenDatabaseRejectsOverlap() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("overlap"));

        // when / then
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:00:00Z", 1)))
                .isInstanceOf(ReservationConflictException.class);
    }

    private static CreateReservationRequest request(String start, int hours) {
        return new CreateReservationRequest(1L, OffsetDateTime.parse(start), hours);
    }
}
