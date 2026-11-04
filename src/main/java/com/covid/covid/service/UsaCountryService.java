package com.covid.covid.service;

import com.covid.covid.model.UsaCountryModel;
import com.covid.covid.repository.UsaCountryRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class UsaCountryService {
    private final UsaCountryRepository repository;

    public UsaCountryService(UsaCountryRepository repository) {
        this.repository = repository;
    }

    public List<UsaCountryModel> getPage(int page, int size) throws IOException {
        return repository.findPage(page, size);
    }
}

