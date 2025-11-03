package tqs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tqs.services.ExternalMunicipalityClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalMunicipalityClientUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private ExternalMunicipalityClient client;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        client = new ExternalMunicipalityClient(restTemplateBuilder);
    }

    @Test
    void whenFetchMunicipalities_thenReturnsList() {
        String[] mockResponse = { "LISBOA", "PORTO", "BRAGA" };
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(mockResponse);

        var result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("LISBOA"));
    }

    @Test
    void whenApiReturnsNull_thenReturnEmptyList() {
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(null);

        var result = client.fetchMunicipalityNamesRaw();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenApiFails_thenThrowsRuntimeException() {
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenThrow(new RestClientException("API Error"));

        assertThrows(RuntimeException.class, () -> client.fetchMunicipalityNamesRaw());
    }
}