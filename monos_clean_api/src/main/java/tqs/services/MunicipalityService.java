package tqs.services;

import org.springframework.stereotype.Service;

import tqs.dto.MunicipalityDTO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MunicipalityService {

    private final ExternalMunicipalityClient client;
    private volatile List<MunicipalityDTO> cachedMunicipalities = Collections.emptyList();
    private volatile long cacheExpiry = 0;
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1);

    public MunicipalityService(ExternalMunicipalityClient client) {
        this.client = client;
    }

    private void refreshCacheIfNeeded() {
        if (cachedMunicipalities == null || System.currentTimeMillis() > cacheExpiry) {
            synchronized (this) {
                if (cachedMunicipalities == null || System.currentTimeMillis() > cacheExpiry) {
                    List<String> names = client.fetchMunicipalityNamesRaw();
                    this.cachedMunicipalities = names.stream()
                            .map(name -> new MunicipalityDTO(name))
                            .collect(Collectors.toList());
                    this.cacheExpiry = System.currentTimeMillis() + CACHE_TTL_MS;
                }
            }
        }
    }

    public boolean isValid(String code) {
        if (code == null)
            return false;
        refreshCacheIfNeeded();
        return cachedMunicipalities.stream()
                .anyMatch(dto -> dto.getCode().equals(code.toUpperCase()));
    }

    public List<MunicipalityDTO> getAllMunicipalities() {
        refreshCacheIfNeeded();
        return cachedMunicipalities;
    }
}