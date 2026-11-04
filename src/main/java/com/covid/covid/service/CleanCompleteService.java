package com.covid.covid.service;
import com.covid.covid.model.CleanCompleteModel;

import com.covid.covid.repository.CleanCompleteRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;

import java.util.List;
@Service
public class CleanCompleteService {

private final CleanCompleteRepository repository;
public CleanCompleteService(CleanCompleteRepository repository){this.repository=repository;}

public List<CleanCompleteModel> getPage(int page,int size)throws IOException{return repository.findPage(page,size);}
}
