package com.covid.covid.repository;
import com.covid.covid.model.WorldometerModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException; import java.util.List;
@Repository
public class WorldometerRepository {
 private final CsvDatasetService csv;
 public WorldometerRepository(CsvDatasetService csv) { this.csv=csv; }
 public List<WorldometerModel> findPage(int page,int size) throws IOException {
  return csv.read("worldometer_data.csv",page,size).stream().map(WorldometerModel::new).toList();
 }
}
