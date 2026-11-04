package com.covid.covid.model;
import java.util.Map;
public class WorldometerModel {
    private final Map<String, String> values;
    public WorldometerModel(Map<String, String> values) { this.values = values; }
    public Map<String, String> getValues() { return values; }
}
