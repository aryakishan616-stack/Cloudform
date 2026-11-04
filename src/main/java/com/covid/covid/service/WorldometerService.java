package com.covid.covid.service;
import com.covid.covid.model.WorldometerModel;

import com.covid.covid.repository.WorldometerRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
@Service
public class WorldometerService {
    private final WorldometerRepository repository;

    public WorldometerService(WorldometerRepository repository) {
        this.repository = repository;
    }

    public List<WorldometerModel> getPage(int page, int size) throws IOException {
        return repository.findPage(page, size);
    }
}
