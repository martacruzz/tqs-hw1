package tqs.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import tqs.dto.MunicipalityDTO;
import tqs.services.ExternalMunicipalityClient;
import tqs.services.MunicipalityService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@ActiveProfiles("test")
class MunicipalityServiceIntegrationTest {

    @MockBean
    private ExternalMunicipalityClient client;

    private MunicipalityService service;
    private final List<String> sampleMunicipalities = Arrays.asList("LISBOA", "PORTO", "BRAGA");

    @BeforeEach
    void setUp() {
        service = new MunicipalityService(client);
    }

    @Test
    void whenCacheExpires_thenRefreshCache() throws InterruptedException {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        // First call to populate cache
        List<MunicipalityDTO> result1 = service.getAllMunicipalities();
        List<String> codes1 = result1.stream()
                .map(MunicipalityDTO::getCode)
                .collect(Collectors.toList());

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
        List<String> codes2 = result2.stream()
                .map(MunicipalityDTO::getCode)
                .collect(Collectors.toList());

        verify(client, times(2)).fetchMunicipalityNamesRaw();

        // Compare the municipality codes instead of the DTO objects
        assertEquals(codes1.size(), codes2.size());
        assertTrue(codes1.containsAll(codes2));
        assertTrue(codes2.containsAll(codes1));
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
                // Verify the content of the municipalities
                List<String> codes = result.stream()
                        .map(MunicipalityDTO::getCode)
                        .collect(Collectors.toList());
                assertTrue(codes.containsAll(sampleMunicipalities));
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