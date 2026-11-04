package com.covid.covid.repository;

import com.covid.covid.model.CovidModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CovidRepository extends JpaRepository<CovidModel, Long> {
    Optional<CovidModel> findFirstByCountryRegionIgnoreCase(String CountryRegion);

    List<CovidModel> findByCountryRegionContainingIgnoreCase(String CountryRegion);
}
