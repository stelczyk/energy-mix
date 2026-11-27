package com.energymix.reservation;

import com.energymix.charger.Charger;
import com.energymix.charger.ChargerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private static final UUID CLIENT_ID = UUID.randomUUID();

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneId.of("Europe/London"));
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ChargerRepository chargerRepository = mock(ChargerRepository.class);
    private final ReservationService service = new ReservationService(reservationRepository, chargerRepository, clock);

    @Test
    void shouldCreateReservation_interpretingStartAsUkTime() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReservationResponse result = service.create(CLIENT_ID, request("2026-07-01T14:00:00", 3));

        // then
        assertThat(result.startTime()).isEqualTo(Instant.parse("2026-07-01T13:00:00Z")); // 14:00 BST = 13:00 UTC
        assertThat(result.endTime()).isEqualTo(Instant.parse("2026-07-01T16:00:00Z"));
    }

    @Test
    void shouldReject_whenStartIsInThePast() {
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T09:00:00", 1)))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("past");
    }

    @Test
    void shouldAllowStart_onHalfHour() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReservationResponse result = service.create(CLIENT_ID, request("2026-07-01T14:30:00", 2));

        // then
        assertThat(result.endTime()).isEqualTo(Instant.parse("2026-07-01T15:30:00Z"));
    }

    @Test
    void shouldReject_whenStartIsNotOnFullOrHalfHour() {
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:15:00", 1)))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("full or half hour");
    }

    @Test
    void shouldReject_whenChargerDoesNotExist() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:00:00", 1)))
                .isInstanceOf(ChargerNotFoundException.class);
    }

    @Test
    void shouldThrowConflict_whenDatabaseRejectsOverlap() {
        // given
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(mock(Charger.class)));
        when(reservationRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("overlap"));

        // when / then
        assertThatThrownBy(() -> service.create(CLIENT_ID, request("2026-07-01T14:00:00", 1)))
                .isInstanceOf(ReservationConflictException.class);
    }

    @Test
    void shouldQueryUpcomingReservations_fromCurrentTime() {
        // when
        service.findUpcoming(CLIENT_ID);

        // then
        verify(reservationRepository)
                .findByClientIdAndEndTimeAfterOrderByStartTime(CLIENT_ID, Instant.parse("2026-07-01T10:00:00Z"));
    }

    @Test
    void shouldNotDelete_whenReservationIsNotClients() {
        // given
        when(reservationRepository.findByIdAndClientId(5L, CLIENT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.cancel(CLIENT_ID, 5L))
                .isInstanceOf(ReservationNotFoundException.class);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void shouldQueryOccupiedSlots_withinUkDay() {
        // when - summer: UK day 2 July starts at 23:00 UTC on 1 July
        service.findOccupied(1L, LocalDate.parse("2026-07-02"), CLIENT_ID);

        // then
        verify(reservationRepository).findByChargerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTime(
                1L, Instant.parse("2026-07-02T23:00:00Z"), Instant.parse("2026-07-01T23:00:00Z"));
    }

    private static CreateReservationRequest request(String start, int hours) {
        return new CreateReservationRequest(1L, LocalDateTime.parse(start), hours);
    }
}
