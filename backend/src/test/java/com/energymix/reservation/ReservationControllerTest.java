package com.energymix.reservation;

import com.energymix.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ReservationControllerTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String OTHER_CLIENT_ID = UUID.randomUUID().toString();

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ReservationRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateReservation() {
        assertThat(book(1, "2099-01-01T10:00:00Z", 2))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.endTime").isEqualTo("2099-01-01T12:00:00Z");
    }

    @Test
    void shouldReturnConflict_whenReservationsOverlapOnSameCharger() {
        // given
        assertThat(book(1, "2099-01-01T10:00:00Z", 2)).hasStatus(HttpStatus.CREATED);

        // when / then
        assertThat(book(1, "2099-01-01T11:00:00Z", 2))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.detail").isEqualTo("This charger is already booked for the selected time.");
    }

    @Test
    void shouldAllowReservation_whenItStartsWhenPreviousEnds() {
        // given
        assertThat(book(1, "2099-01-01T10:00:00Z", 2)).hasStatus(HttpStatus.CREATED);

        // when / then
        assertThat(book(1, "2099-01-01T12:00:00Z", 1)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void shouldAllowSameTime_onDifferentCharger() {
        // given
        assertThat(book(1, "2099-01-01T10:00:00Z", 2)).hasStatus(HttpStatus.CREATED);

        // when / then
        assertThat(book(2, "2099-01-01T10:00:00Z", 2)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void shouldReturnBadRequest_whenHoursOutOfRange() {
        assertThat(book(1, "2099-01-01T10:00:00Z", 7)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequest_whenClientIdMissing() {
        assertThat(mvc.post().uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(1, "2099-01-01T10:00:00Z", 1)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldListReservations_onlyForTheirOwner() {
        // given
        book(1, "2099-01-01T10:00:00Z", 2);

        // when / then
        assertThat(listFor(CLIENT_ID)).bodyJson().extractingPath("$[*].chargerName").asArray()
                .containsExactly("King's Cross Hub");
        assertThat(listFor(OTHER_CLIENT_ID)).bodyJson().extractingPath("$").asArray().isEmpty();
    }

    @Test
    void shouldReturnNotFound_whenCancellingSomeoneElsesReservation() {
        // given
        Long id = createAndGetId();

        // when / then
        assertThat(cancel(OTHER_CLIENT_ID, id)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(repository.existsById(id)).isTrue();
    }

    @Test
    void shouldFreeTheSlot_whenOwnerCancels() {
        // given
        Long id = createAndGetId();

        // when
        assertThat(cancel(CLIENT_ID, id)).hasStatus(HttpStatus.NO_CONTENT);

        // then
        assertThat(listFor(CLIENT_ID)).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(book(1, "2099-01-01T10:00:00Z", 2)).hasStatus(HttpStatus.CREATED);
    }

    private Long createAndGetId() {
        book(1, "2099-01-01T10:00:00Z", 2);
        return repository.findAll().getFirst().getId();
    }

    private MvcTestResult listFor(String clientId) {
        return mvc.get().uri("/api/v1/reservations").header("X-Client-Id", clientId).exchange();
    }

    private MvcTestResult cancel(String clientId, Long id) {
        return mvc.delete().uri("/api/v1/reservations/{id}", id).header("X-Client-Id", clientId).exchange();
    }

    private MvcTestResult book(long chargerId, String start, int hours) {
        return mvc.post().uri("/api/v1/reservations")
                .header("X-Client-Id", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(chargerId, start, hours))
                .exchange();
    }

    private static String body(long chargerId, String start, int hours) {
        return """
                {"chargerId": %d, "startTime": "%s", "hours": %d}
                """.formatted(chargerId, start, hours);
    }
}
