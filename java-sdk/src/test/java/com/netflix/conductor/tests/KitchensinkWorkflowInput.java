package com.netflix.conductor.tests;

public class KitchensinkWorkflowInput {

    private String name;

    private String zipCode;

    private String countryCode;

    public KitchensinkWorkflowInput(String name, String zipCode, String countryCode) {
        this.name = name;
        this.zipCode = zipCode;
        this.countryCode = countryCode;
    }

    public KitchensinkWorkflowInput() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
