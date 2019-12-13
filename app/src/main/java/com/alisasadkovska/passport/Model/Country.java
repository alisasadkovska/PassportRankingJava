package com.alisasadkovska.passport.Model;

public class Country {
    private String countryName;
    private Long visaStatus;

    public Country() {
    }

    public Country(String countryName, Long visaStatus) {
        this.countryName = countryName;
        this.visaStatus = visaStatus;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Long getVisaStatus() {
        return visaStatus;
    }

    public void setVisaStatus(Long visaStatus) {
        this.visaStatus = visaStatus;
    }
}
