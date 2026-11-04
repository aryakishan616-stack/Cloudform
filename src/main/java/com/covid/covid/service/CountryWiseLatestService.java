package com.covid.covid.service;
import com.covid.covid.model.CountryWiseLatestModel;
import com.covid.covid.repository.CountryWiseLatestRepository;
import org.springframework.stereotype.Service;
import java.io.IOException; import java.util.List;
@Service
public class CountryWiseLatestService {
 private final CountryWiseLatestRepository repository;
 public CountryWiseLatestService(CountryWiseLatestRepository repository){this.repository=repository;}
 public List<CountryWiseLatestModel> getPage(int page,int size)throws IOException{return repository.findPage(page,size);}
}
