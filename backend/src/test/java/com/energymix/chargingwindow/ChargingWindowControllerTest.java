package com.energymix.chargingwindow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(ChargingWindowController.class)
class ChargingWindowControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ChargingWindowService service;

    @Test
    void shouldReturnWindow_whenHoursAreValid() {
        // given
        when(service.findCleanestWindow(3)).thenReturn(new ChargingWindow(
                OffsetDateTime.parse("2026-11-28T13:00Z"), OffsetDateTime.parse("2026-11-28T16:00Z"), 72.5));

        // when & then
        assertThat(mvc.get().uri("/api/v1/energy/charging-window?hours=3"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"start":"2026-11-28T13:00:00Z","end":"2026-11-28T16:00:00Z","cleanPercentage":72.5}
                        """);
    }

    @Test
    void shouldReturn400_whenHoursOutOfRange() {
        // when & then
        assertThat(mvc.get().uri("/api/v1/energy/charging-window?hours=7"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
