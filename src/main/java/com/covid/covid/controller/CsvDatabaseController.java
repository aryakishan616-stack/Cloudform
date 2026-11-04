package com.covid.covid.controller;

import com.covid.covid.service.CsvDatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/covid/database")
public class CsvDatabaseController {
    private final CsvDatabaseService databaseService;

    public CsvDatabaseController(CsvDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping("/import/{dataset}")
    public ResponseEntity<?> importDataset(
            @org.springframework.web.bind.annotation.PathVariable String dataset) {
        String fileName = switchDataset(dataset);
        try {
            return ResponseEntity.ok(databaseService.importFile(fileName));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping("/{dataset}")
    public ResponseEntity<?> read(
            @org.springframework.web.bind.annotation.PathVariable String dataset,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            String fileName = switchDataset(dataset);
            return ResponseEntity.ok(databaseService.getPage(fileName, page, size));
        } catch (IllegalArgumentException | IOException exception) {
            if (exception instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(exception.getMessage());
            }
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    private String switchDataset(String dataset) {
        return switch (dataset) {
            case "country-wise-latest" -> "country_wise_latest.csv";
            case "clean-complete" -> "covid_19_clean_complete.csv";
            case "day-wise" -> "day_wise.csv";
            case "full-grouped" -> "full_grouped.csv";
            case "usa-counties" -> "usa_county_wise.csv";
            case "worldometer" -> "worldometer_data.csv";
            default -> throw new IllegalArgumentException("Unknown dataset: " + dataset);
        };
    }
}
