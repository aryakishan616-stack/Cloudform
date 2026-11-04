package com.covid.covid.model;
import java.util.Map;
public class CleanCompleteModel {
    private final Map<String, String> values;
    public CleanCompleteModel(Map<String, String> values) { this.values = values; }
    public Map<String, String> getValues() { return values; }
}
