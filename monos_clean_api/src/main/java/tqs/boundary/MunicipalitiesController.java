// tqs/boundary/MunicipalitiesController.java
package tqs.boundary;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tqs.services.MunicipalityService;
import tqs.dto.MunicipalityDTO;

@RestController
@RequestMapping("/api")
public class MunicipalitiesController {

    private final MunicipalityService service;

    public MunicipalitiesController(MunicipalityService service) {
        this.service = service;
    }

    @GetMapping("/municipalities")
    public List<MunicipalityDTO> getMunicipalities() {
        return service.getAllMunicipalities();
    }
}