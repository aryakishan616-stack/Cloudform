package com.covid.covid.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class CsvDatabaseRepository {
    private final JdbcTemplate jdbcTemplate;

    public CsvDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int replace(String tableName, List<Map<String, String>> rows) {
        String safeTable = tableName(tableName);
        if (rows.isEmpty()) {
            return 0;
        }

        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        List<String> safeColumns = columns.stream().map(this::columnName).toList();

        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + safeTable + "`");
        String definitions = String.join(", ", safeColumns.stream()
                .map(column -> "`" + column + "` LONGTEXT")
                .toList());
        jdbcTemplate.execute("CREATE TABLE `" + safeTable + "` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + definitions + ")");

        String placeholders = String.join(", ", Collections.nCopies(safeColumns.size(), "?"));
        String sql = "INSERT INTO `" + safeTable + "` (`"
                + String.join("`, `", safeColumns) + "`) VALUES (" + placeholders + ")";

        List<Object[]> batch = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Object[] values = columns.stream().map(column -> row.get(column)).toArray();
            batch.add(values);
        }
        return batch.stream().mapToInt(values ->
                jdbcTemplate.update(sql, values)).sum();
    }

    public int append(String tableName, List<Map<String, String>> rows) {
        String safeTable = tableName(tableName);
        if (rows.isEmpty()) {
            return 0;
        }

        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        List<String> safeColumns = columns.stream().map(this::columnName).toList();
        String placeholders = String.join(", ", Collections.nCopies(safeColumns.size(), "?"));
        String sql = "INSERT INTO `" + safeTable + "` (`"
                + String.join("`, `", safeColumns) + "`) VALUES (" + placeholders + ")";

        int imported = 0;
        for (Map<String, String> row : rows) {
            Object[] values = columns.stream().map(column -> row.get(column)).toArray();
            imported += jdbcTemplate.update(sql, values);
        }
        return imported;
    }

    public List<Map<String, Object>> read(String tableName, int page, int size) {
        String safeTable = tableName(tableName);
        return jdbcTemplate.queryForList(
                "SELECT * FROM `" + safeTable + "` LIMIT ? OFFSET ?",
                size,
                page * size);
    }

    private String tableName(String name) {
        String safe = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        if (safe.isBlank()) {
            throw new IllegalArgumentException("Invalid table name");
        }
        return "csv_" + safe;
    }

    private String columnName(String name) {
        String safe = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        return safe.isBlank() ? "column_" + System.identityHashCode(name) : safe;
    }
}
