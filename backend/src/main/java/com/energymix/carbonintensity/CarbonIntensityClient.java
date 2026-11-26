package com.energymix.carbonintensity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Instant;
import java.util.List;

@Component
public class CarbonIntensityClient {

    private final RestClient restClient;

    public CarbonIntensityClient(RestClient.Builder builder,
                                 @Value("${carbon-intensity.base-url}") String baseUrl) {
        // API rejects percent-encoded ':' in timestamps (400); URI_COMPONENT encodes only illegal chars
        var uriFactory = new DefaultUriBuilderFactory(baseUrl);
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        this.restClient = builder.uriBuilderFactory(uriFactory).build();
    }

    public List<GenerationInterval> getGeneration(Instant from, Instant to) {
        GenerationResponse response = restClient.get()
                .uri("/generation/{from}/{to}", from, to)
                .retrieve()
                .body(GenerationResponse.class);
        return response == null || response.data() == null ? List.of() : response.data();
    }
}
