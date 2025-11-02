package tqs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ExternalMunicipalityClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalMunicipalityClient.class);
    private static final String MUNICIPALITY_API_URL = "https://json.geoapi.pt/municipio";

    private final RestTemplate restTemplate;

    public ExternalMunicipalityClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<String> fetchMunicipalityNamesRaw() {
        try {
            String[] response = restTemplate.getForObject(MUNICIPALITY_API_URL, String[].class);
            if (response == null)
                return List.of();
            return Arrays.asList(response);
        } catch (RestClientException e) {
            logger.error("Failed to fetch municipalities", e);
            throw new RuntimeException("Unable to fetch municipality list", e);
        }
    }
}