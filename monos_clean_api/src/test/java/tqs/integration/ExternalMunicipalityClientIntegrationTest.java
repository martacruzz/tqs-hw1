package tqs.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tqs.services.ExternalMunicipalityClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ExternalMunicipalityClientIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }
    }

    @Autowired
    private ExternalMunicipalityClient client;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void whenFetchMunicipalities_withRealSpringContext_thenReturnsList() {
        String[] mockResponse = { "LISBOA", "PORTO", "BRAGA" };
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(mockResponse);

        List<String> result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("LISBOA"));
    }

    @Test
    void whenApiReturnsNull_withRealSpringContext_thenReturnEmptyList() {
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(null);

        List<String> result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenApiFails_withRealSpringContext_thenThrowsRuntimeException() {
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenThrow(new RestClientException("API Error"));

        assertThrows(RuntimeException.class, () -> client.fetchMunicipalityNamesRaw());
    }
}