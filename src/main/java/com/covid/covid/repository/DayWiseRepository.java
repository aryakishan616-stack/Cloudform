package com.covid.covid.repository;
import com.covid.covid.model.DayWiseModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.List;
@Repository
public class DayWiseRepository {
 private final CsvDatasetService csv;
 public DayWiseRepository(CsvDatasetService csv) { this.csv=csv; }
 public List<DayWiseModel> findPage(int page,int size) throws IOException {
  return csv.read("day_wise.csv",page,size).stream().map(DayWiseModel::new).toList();
 }
}
