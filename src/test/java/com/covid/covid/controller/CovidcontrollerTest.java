package com.covid.covid.controller;

import com.covid.covid.model.CovidModel;
import com.covid.covid.repository.CovidRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CovidcontrollerTest {
    private final CovidRepository covidRepository = mock(CovidRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new CovidController(covidRepository)
    ).build();

    @Test
    void updateExistingCountry() throws Exception {
        CovidModel existing = new CovidModel("Example", 100L, 5L, 80L, 15L);
        when(covidRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(covidRepository.save(any(CovidModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/covid/country/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countryRegion":"Example",
                                  "confirmed":120,
                                  "deaths":4,
                                  "recovered":90,
                                  "active":26
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryRegion").value("Example"))
                .andExpect(jsonPath("$.confirmed").value(120));

        verify(covidRepository).save(existing);
    }

    @Test
    void updateMissingCountryReturnsNotFound() throws Exception {
        when(covidRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/covid/country/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countryRegion":"Missing",
                                  "confirmed":1,
                                  "deaths":0,
                                  "recovered":1,
                                  "active":0
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRejectsNegativeCounts() throws Exception {
        when(covidRepository.findById(1L)).thenReturn(Optional.of(
                new CovidModel("Example", 100L, 5L, 80L, 15L)));

        mockMvc.perform(put("/api/covid/country/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countryRegion":"Example",
                                  "confirmed":-1,
                                  "deaths":0,
                                  "recovered":0,
                                  "active":0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteExistingCountry() throws Exception {
        when(covidRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/covid/country/1"))
                .andExpect(status().isNoContent());

        verify(covidRepository).deleteById(1L);
    }

    @Test
    void deleteMissingCountryReturnsNotFound() throws Exception {
        when(covidRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/covid/country/999"))
                .andExpect(status().isNotFound());
    }
}
