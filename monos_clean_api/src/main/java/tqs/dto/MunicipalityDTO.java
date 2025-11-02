package tqs.dto;

public class MunicipalityDTO {
    private String code; // "LISBOA"
    private String name; // "Lisboa"

    public MunicipalityDTO(String name) {
        this.name = name;
        this.code = name.toUpperCase();
    }

    // getters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}