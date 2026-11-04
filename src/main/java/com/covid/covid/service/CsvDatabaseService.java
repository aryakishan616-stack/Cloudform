package com.covid.covid.service;

import com.covid.covid.repository.CsvDatabaseRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CsvDatabaseService {
    private final CsvDatasetService csvDatasetService;
    private final CsvDatabaseRepository databaseRepository;

    public CsvDatabaseService(
            CsvDatasetService csvDatasetService,
            CsvDatabaseRepository databaseRepository) {
        this.csvDatasetService = csvDatasetService;
        this.databaseRepository = databaseRepository;
    }

    public int importFile(String fileName) throws IOException {
        int imported = 0;
        int page = 0;
        while (true) {
            List<Map<String, String>> rows = csvDatasetService.read(fileName, page, 5000);
            if (rows.isEmpty()) {
                break;
            }
            if (page == 0) {
                imported += databaseRepository.replace(fileName, rows);
            } else {
                imported += databaseRepository.append(fileName, rows);
            }
            page++;
        }
        return imported;
    }

    public List<Map<String, String>> getPage(String fileName, int page, int size) throws IOException {
        return csvDatasetService.read(fileName, page, size);
    }
}



