package com.covid.covid.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class CsvDatasetModel {
    private final Map<String, String> values;

    public CsvDatasetModel(Map<String, String> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public Map<String, String> getValues() {
        return values;
    }
}
