package com.covid.covid.repository;
import com.covid.covid.model.CleanCompleteModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException; import java.util.List;
@Repository
public class CleanCompleteRepository {
private final CsvDatasetService csv;
public CleanCompleteRepository(CsvDatasetService csv) { this.csv=csv; }
public List<CleanCompleteModel> findPage(int page,int size) throws IOException {
return csv.read("covid_19_clean_complete.csv",page,size).stream().map(CleanCompleteModel::new).toList();
}
}
