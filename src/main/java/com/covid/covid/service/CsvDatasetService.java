package com.covid.covid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvDatasetService {

    private final Path csvDirectory;

    public CsvDatasetService(@Value("${app.csv.directory:..}") String csvDirectory) {
        this.csvDirectory = Path.of(csvDirectory).toAbsolutePath().normalize();
    }

    public List<Map<String, String>> read(String fileName, int page, int size)
            throws IOException {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > 5000) {
            throw new IllegalArgumentException("size must be between 1 and 5000");
        }

        Path csvFile = csvDirectory.resolve(fileName).normalize();
        if (!Files.isRegularFile(csvFile)) {
            throw new IOException("CSV file not found: " + csvFile);
        }

        int start = page * size;
        int end = start + size;
        int currentRow = 0;
        List<Map<String, String>> result = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return result;
            }

            List<String> headers = parseCsvLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null && currentRow < end) {
                if (line.isBlank()) {
                    continue;
                }

                if (currentRow >= start) {
                    List<String> values = parseCsvLine(line);
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int index = 0; index < headers.size(); index++) {
                        String value = index < values.size() ? values.get(index) : "";
                        row.put(headers.get(index), value);
                    }
                    result.add(row);
                }
                currentRow++;
            }
        }

        return result;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        values.add(value.toString().trim());
        return values;
    }
}