package com.covid.covid.service;

import com.covid.covid.model.CovidModel;
import com.covid.covid.repository.CovidRepository;
import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CovidLiveService implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(CovidLiveService.class);
    private static final String LIVE_API_URL = "https://disease.sh/v3/covid-19/countries";

    private final CovidRepository repository;
    private final RestTemplate restTemplate;

    public CovidLiveService(CovidRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    @Bean
    public RestTemplate restTemplate() {
        return restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            refreshFromLiveApi();
        } catch (RuntimeException exception) {
            logger.warn("Live data is unavailable during startup; existing database data will be used");
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshScheduled() {
        refreshFromLiveApi();
    }

    @Transactional
    public int refreshFromLiveApi() {
        try {
            JsonNode response = restTemplate.getForObject(LIVE_API_URL, JsonNode.class);
            if (response == null || !response.isArray()) {
                throw new IllegalStateException("Live COVID API returned an invalid response");
            }

            List<CovidModel> countries = new ArrayList<>();
            for (JsonNode item : response) {
                String country = text(item, "country");
                if (country == null || country.isBlank()) {
                    continue;
                }

                long confirmed = longValue(item, "cases");
                long deaths = longValue(item, "deaths");
                long recovered = longValue(item, "recovered");
                long active = longValue(item, "active");
                if (active == 0) {
                    active = Math.max(confirmed - deaths - recovered, 0);
                }

                countries.add(new CovidModel(country, confirmed, deaths, recovered, active));
            }

            if (!countries.isEmpty()) {
                repository.deleteAll();
                repository.saveAll(countries);
                logger.info("Updated COVID dashboard with live data for {} countries", countries.size());
            }

            return countries.size();
        } catch (Exception exception) {
            logger.warn("Could not refresh live COVID data: {}", exception.getMessage());
            throw new RuntimeException("Could not refresh live COVID data", exception);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? 0L : value.asLong(0L);
    }
}
