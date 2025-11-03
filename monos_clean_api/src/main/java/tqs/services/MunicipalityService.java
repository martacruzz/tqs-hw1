package tqs.services;

import org.springframework.stereotype.Service;

import tqs.dto.MunicipalityDTO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MunicipalityService {
    private final ExternalMunicipalityClient client;
    private final AtomicReference<List<MunicipalityDTO>> cachedMunicipalities;
    private volatile long cacheExpiry = 0;
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1);

    public MunicipalityService(ExternalMunicipalityClient client) {
        this.client = client;
        this.cachedMunicipalities = new AtomicReference<>(Collections.emptyList());
    }

    private void refreshCacheIfNeeded() {
        if (System.currentTimeMillis() > cacheExpiry || cachedMunicipalities.get().isEmpty()) {
            synchronized (this) {
                if (System.currentTimeMillis() > cacheExpiry || cachedMunicipalities.get().isEmpty()) {
                    List<String> names = client.fetchMunicipalityNamesRaw();
                    List<MunicipalityDTO> municipalities = names.stream()
                            .map(MunicipalityDTO::new)
                            .collect(Collectors.toUnmodifiableList());
                    cachedMunicipalities.set(municipalities);
                    cacheExpiry = System.currentTimeMillis() + CACHE_TTL_MS;
                }
            }
        }
    }

    public boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        refreshCacheIfNeeded();
        return cachedMunicipalities.get().stream()
                .anyMatch(dto -> dto.getCode().equals(code.toUpperCase()));
    }

    public List<MunicipalityDTO> getAllMunicipalities() {
        refreshCacheIfNeeded();
        return cachedMunicipalities.get();
    }
}