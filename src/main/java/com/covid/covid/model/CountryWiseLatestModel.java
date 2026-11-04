package com.covid.covid.model;
import java.util.Map;
public class CountryWiseLatestModel {
    private final Map<String, String> values;
    public CountryWiseLatestModel(Map<String, String> values) { this.values = values; }
    public Map<String, String> getValues() { return values; }
}
