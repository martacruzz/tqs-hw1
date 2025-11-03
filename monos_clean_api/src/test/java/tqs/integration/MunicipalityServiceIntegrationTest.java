package tqs.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import tqs.dto.MunicipalityDTO;
import tqs.services.ExternalMunicipalityClient;
import tqs.services.MunicipalityService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class MunicipalityServiceIntegrationTest {

    @MockBean
    private ExternalMunicipalityClient client;

    @Autowired
    private MunicipalityService service;

    private final List<String> sampleMunicipalities = Arrays.asList("LISBOA", "PORTO", "BRAGA");

    @Test
    void whenCacheExpires_thenRefreshCache() throws InterruptedException {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        // First call to populate cache
        List<MunicipalityDTO> result1 = service.getAllMunicipalities();

        // Force cache expiry
        try {
            java.lang.reflect.Field cacheExpiryField = MunicipalityService.class.getDeclaredField("cacheExpiry");
            cacheExpiryField.setAccessible(true);
            cacheExpiryField.set(service, System.currentTimeMillis() - 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not manipulate cache expiry");
        }

        // Second call should refresh cache
        List<MunicipalityDTO> result2 = service.getAllMunicipalities();

        verify(client, times(2)).fetchMunicipalityNamesRaw();
        assertEquals(result1, result2);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                List<MunicipalityDTO> result = service.getAllMunicipalities();
                assertNotNull(result);
                assertEquals(3, result.size());
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }
}