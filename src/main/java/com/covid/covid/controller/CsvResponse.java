package com.covid.covid.controller;

import com.covid.covid.service.CsvDatasetService;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

final class CsvResponse {
    private CsvResponse() {
    }

    static ResponseEntity<?> read(
            CsvDatasetService csvDatasetService,
            String fileName,
            int page,
            int size) {
        try {
            return ResponseEntity.ok(csvDatasetService.read(fileName, page, size));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }
}
