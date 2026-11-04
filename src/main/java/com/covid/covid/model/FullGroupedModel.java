package com.covid.covid.model;
import java.util.Map;
public class FullGroupedModel {
    private final Map<String, String> values;
    public FullGroupedModel(Map<String, String> values) { this.values = values; }
    public Map<String, String> getValues() { return values; }
}
