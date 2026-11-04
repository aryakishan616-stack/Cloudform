package com.covid.covid.repository;
import com.covid.covid.model.CountryWiseLatestModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException; import java.util.List;
@Repository
public class CountryWiseLatestRepository {
 private final CsvDatasetService csv;
 public CountryWiseLatestRepository(CsvDatasetService csv) { this.csv=csv; }
 public List<CountryWiseLatestModel> findPage(int page,int size) throws IOException {
  return csv.read("country_wise_latest.csv",page,size).stream().map(CountryWiseLatestModel::new).toList();
 }
}
