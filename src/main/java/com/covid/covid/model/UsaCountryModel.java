package com.covid.covid.model;
import java.util.Map;
public class UsaCountryModel {
    private final Map<String, String> values;
    public UsaCountryModel(Map<String, String> values) { this.values =values; }
    public Map<String, String> getValues() { return values; }
}