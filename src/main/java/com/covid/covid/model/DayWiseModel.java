package com.covid.covid.model;
import java.util.Map;
public class DayWiseModel {
    private final Map<String, String> values;
    public DayWiseModel(Map<String, String> values) { this.values = values; }
    public Map<String, String> getValues() { return values; }
}
