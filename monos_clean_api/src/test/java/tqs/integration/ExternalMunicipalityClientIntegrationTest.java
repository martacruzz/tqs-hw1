package tqs.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tqs.services.ExternalMunicipalityClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@ActiveProfiles("test")
class ExternalMunicipalityClientIntegrationTest {

    private static final String API_URL = "https://json.geoapi.pt/municipio";

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        @Primary
        public RestTemplateBuilder restTemplateBuilder() {
            RestTemplate mockRestTemplate = restTemplate();
            RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
            when(builder.build()).thenReturn(mockRestTemplate);
            return builder;
        }
    }

    @Autowired
    private ExternalMunicipalityClient client;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void whenFetchMunicipalities_withRealSpringContext_thenReturnsList() {
        String[] mockResponse = { "LISBOA", "PORTO", "BRAGA" };
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(mockResponse);

        List<String> result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("LISBOA"));
    }

    @Test
    void whenApiReturnsNull_withRealSpringContext_thenReturnEmptyList() {
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(null);

        List<String> result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenApiFails_withRealSpringContext_thenThrowsRuntimeException() {
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenThrow(new RestClientException("API Error"));

        assertThrows(RuntimeException.class, () -> client.fetchMunicipalityNamesRaw());
    }
}