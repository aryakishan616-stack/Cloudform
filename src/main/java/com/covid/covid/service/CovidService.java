package com.covid.covid.service;
import org.springframework.stereotype.Service;
import com.covid.covid.model.CovidModel;
import com.covid.covid.repository.CovidRepository;
import java.util.List;
import java.util.Optional;
@Service
public class CovidService {
    private final CovidRepository Covidrepository;
    public CovidService(CovidRepository Covidrepository) {
        this.Covidrepository = Covidrepository;
    }

    public List<CovidModel> getallCovidStats() {
        return Covidrepository.findAll();
    }

    public CovidModel addserviceConfirmed(CovidModel covidmodel){
        return Covidrepository.save(covidmodel);
    }

    public Optional<CovidModel> getCovidState(Long id) {
        return Covidrepository.findById(id);
    }

    public Optional<CovidModel> findByCountryRegion(String countryRegion) {
        return Covidrepository.findFirstByCountryRegionIgnoreCase(countryRegion);
    }

    public CovidModel updateCovidState(Long id, CovidModel covidmodel) {
        CovidModel existing = Covidrepository.findById(id).orElseThrow();
        existing.setCountryRegion(covidmodel.getCountryRegion().trim());
        existing.setConfirmed(covidmodel.getConfirmed());
        existing.setDeaths(covidmodel.getDeaths());
        existing.setRecovered(covidmodel.getRecovered());
        existing.setActive(covidmodel.getActive());
        return Covidrepository.save(existing);
    }

    public boolean deleteCovidState(Long id) {
        if (!Covidrepository.existsById(id)) {
            return false;
        }

        Covidrepository.deleteById(id);
        return true;
    }

    public List<CovidModel> searchCovidStats(String countryRegion){
        return Covidrepository.findByCountryRegionContainingIgnoreCase(countryRegion);
    }
}
