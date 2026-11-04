package com.covid.covid.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import com.covid.covid.model.CovidModel;
import com.covid.covid.repository.CovidRepository;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.covid.covid.service.CovidLiveService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/covid")
public class CovidController {

    private final CovidRepository covidrepository;
    private final CovidLiveService liveService;

    public CovidController(CovidRepository covidrepository) {
        this.covidrepository = covidrepository;
        this.liveService = null;
    }

    @Autowired
    public CovidController(CovidRepository covidrepository, CovidLiveService liveService) {
        this.covidrepository = covidrepository;
        this.liveService = liveService;
    }

    @GetMapping("/all")
    public List<CovidModel> getAllCovidStats() {
        return covidrepository.findAll();
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshLiveData() {
        if (liveService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Live COVID refresh service is unavailable");
        }

        try {
            liveService.refreshFromLiveApi();
            return ResponseEntity.ok(covidrepository.findAll());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Could not refresh live COVID data from the external provider");
        }
    }

    @GetMapping("/country/{countryRegion}")
    public ResponseEntity<CovidModel> getCovidState(@PathVariable String countryRegion) {
        return covidrepository.findFirstByCountryRegionIgnoreCase(countryRegion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<CovidModel> addCovidStates(@RequestBody CovidModel covidmodel) {
        if (!isValidCovidStats(covidmodel)) {
            return ResponseEntity.badRequest().build();
        }

        covidmodel.setId(null);
        covidmodel.setCountryRegion(covidmodel.getCountryRegion().trim());
        CovidModel saved = covidrepository.save(covidmodel);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/country/{id}")
    public ResponseEntity<CovidModel> updateCovidState(
            @PathVariable Long id,
            @RequestBody CovidModel covidmodel) {
        if (!isValidCovidStats(covidmodel)) {
            return ResponseEntity.badRequest().build();
        }

        return covidrepository.findById(id)
                .map(existing -> {
                    existing.setCountryRegion(covidmodel.getCountryRegion().trim());
                    existing.setConfirmed(covidmodel.getConfirmed());
                    existing.setDeaths(covidmodel.getDeaths());
                    existing.setRecovered(covidmodel.getRecovered());
                    existing.setActive(covidmodel.getActive());
                    return ResponseEntity.ok(covidrepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/country/{id}")
    public ResponseEntity<Void> deleteCovidState(@PathVariable Long id) {
        if (!covidrepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        covidrepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isValidCovidStats(CovidModel covidmodel) {
        if (covidmodel == null || covidmodel.getCountryRegion() == null
                || covidmodel.getCountryRegion().isBlank()) {
            return false;
        }

        return hasValidCount(covidmodel.getConfirmed())
                && hasValidCount(covidmodel.getDeaths())
                && hasValidCount(covidmodel.getRecovered())
                && hasValidCount(covidmodel.getActive());
    }

    private boolean hasValidCount(Long count) {
        return count != null && count >= 0;
    }

    @GetMapping("/search")
    public List<CovidModel> searchCovidStats(
            @RequestParam(required = false, defaultValue = "") String countryRegion) {
        String searchTerm = countryRegion == null ? "" : countryRegion.trim();
        if (searchTerm.isEmpty()) {
            return covidrepository.findAll();
        }
        return covidrepository.findByCountryRegionContainingIgnoreCase(searchTerm);
    }
}
