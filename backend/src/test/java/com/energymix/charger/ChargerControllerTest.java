package com.energymix.charger;

import com.energymix.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ChargerControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void shouldReturnSeededChargers() {
        assertThat(mvc.get().uri("/api/v1/chargers"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].name")
                .asArray()
                .containsExactly("King's Cross Hub", "Piccadilly Fast Charge", "Waverley Station");
    }
}
