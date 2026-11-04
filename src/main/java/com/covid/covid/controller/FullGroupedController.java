package com.covid.covid.controller;

import com.covid.covid.service.FullGroupedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/covid/datasets/full-grouped")
public class FullGroupedController {
    private final FullGroupedService service;

    public FullGroupedController(FullGroupedService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try { return ResponseEntity.ok(service.getPage(page, size)); }
        catch (IllegalArgumentException | java.io.IOException exception) { return ResponseEntity.badRequest().body(exception.getMessage()); }
    }
}
