package com.covid.covid.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_stats")
public class CovidModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String countryRegion;

    private Long confirmed;

    private Long deaths;

    private Long recovered;

    private Long active;

    public CovidModel() {
    }

    public CovidModel(String countryRegion, Long confirmed,
                        Long deaths, Long recovered, Long active) {
        this.countryRegion = countryRegion;
        this.confirmed =confirmed;
        this.deaths =deaths;
        this.recovered =recovered;
        this.active =active;
        }

    public Long getId() {
    return id;
    }

    public void setId(Long id) {
    this.id =id;
    }

    public String getCountryRegion() {
    return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public Long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Long confirmed) {
        this.confirmed = confirmed;
    }

    public Long getDeaths() {
    return deaths;
    }

    public void setDeaths(Long deaths) {
        this.deaths = deaths;
    }

    public Long getRecovered() {
    return recovered;
    }

    public void setRecovered(Long recovered) {
        this.recovered = recovered;
}

    public Long getActive() {
    return active;
    }

    public void setActive(Long active) {
        this.active = active;
    }
}












