package com.covid.covid.service;
import com.covid.covid.model.DayWiseModel;

import com.covid.covid.repository.DayWiseRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
@Service
public class DayWiseService {

private final DayWiseRepository repository;
public DayWiseService(DayWiseRepository repository){this.repository=repository;}

public List<DayWiseModel> getPage(int page,int size)throws IOException{return repository.findPage(page,size);}
}
