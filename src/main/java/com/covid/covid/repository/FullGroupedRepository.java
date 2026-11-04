package com.covid.covid.repository;
import com.covid.covid.model.FullGroupedModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException; 
import java.util.List;
@Repository
public class FullGroupedRepository {
 private final CsvDatasetService csv;
 public FullGroupedRepository(CsvDatasetService csv) { this.csv=csv; }
 public List<FullGroupedModel> findPage(int page,int size) throws IOException {
  return csv.read("full_grouped.csv",page,size).stream().map(FullGroupedModel::new).toList();
 }
}
