package com.covid.covid.repository;
import com.covid.covid.model.UsaCountryModel;
import com.covid.covid.service.CsvDatasetService;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.List;
@Repository
public class UsaCountryRepository {
private final CsvDatasetService csv;
public UsaCountryRepository(CsvDatasetService csv) { this.csv=csv; }
public List<UsaCountryModel> findPage(int page,int size) throws IOException {
    return csv.read("usa_country.csv",page,size).stream().map(UsaCountryModel::new).toList();
}
}