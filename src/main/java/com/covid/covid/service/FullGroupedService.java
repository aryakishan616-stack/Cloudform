package com.covid.covid.service;
import com.covid.covid.model.FullGroupedModel;

import com.covid.covid.repository.FullGroupedRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
@Service
public class FullGroupedService {

private final FullGroupedRepository repository;
public FullGroupedService(FullGroupedRepository repository){this.repository=repository;}

public List<FullGroupedModel> getPage(int page, int size) throws IOException { return repository.findPage(page, size); }
}