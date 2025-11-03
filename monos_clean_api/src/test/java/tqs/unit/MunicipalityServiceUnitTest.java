package tqs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.dto.MunicipalityDTO;
import tqs.services.ExternalMunicipalityClient;
import tqs.services.MunicipalityService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceUnitTest {

    @Mock
    private ExternalMunicipalityClient client;

    private MunicipalityService service;
    private final List<String> sampleMunicipalities = Arrays.asList("LISBOA", "PORTO", "BRAGA");

    @BeforeEach
    void setUp() {
        service = new MunicipalityService(client);
    }

    @Test
    void whenIsValid_withNullCode_thenReturnFalse() {
        assertFalse(service.isValid(null));
        verify(client, never()).fetchMunicipalityNamesRaw();
    }

    @Test
    void whenIsValid_withValidCode_thenReturnTrue() {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        assertTrue(service.isValid("LISBOA"));
        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }

    @Test
    void whenIsValid_withInvalidCode_thenReturnFalse() {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        assertFalse(service.isValid("INVALID"));
        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }

    @Test
    void whenGetAllMunicipalities_thenReturnList() {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        List<MunicipalityDTO> result = service.getAllMunicipalities();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("LISBOA", result.get(0).getCode());
        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }

    @Test
    void whenCacheExists_thenDontRefetch() {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        // first call to populate cache
        service.getAllMunicipalities();

        // second call should use cache
        service.getAllMunicipalities();

        // client was only called once
        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }

    @Test
    void whenIsValid_withLowercaseCode_thenReturnTrue() {
        when(client.fetchMunicipalityNamesRaw()).thenReturn(sampleMunicipalities);

        assertTrue(service.isValid("lisboa"));
        verify(client, times(1)).fetchMunicipalityNamesRaw();
    }
}