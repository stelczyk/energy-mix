package com.energymix;

import com.energymix.energymix.EnergyMixController;
import com.energymix.energymix.EnergyMixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(EnergyMixController.class)
class WebConfigTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    EnergyMixService service;

    @Test
    void shouldAllowConfiguredOrigin() {
        assertThat(mvc.get().uri("/api/v1/energy/mix").header("Origin", "http://localhost:5173"))
                .hasStatusOk()
                .hasHeader("Access-Control-Allow-Origin", "http://localhost:5173");
    }

    @Test
    void shouldRejectUnknownOrigin() {
        assertThat(mvc.get().uri("/api/v1/energy/mix").header("Origin", "https://evil.example"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}
