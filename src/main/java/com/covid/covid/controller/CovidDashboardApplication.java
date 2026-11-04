package com.covid.covid.controller;

import com.covid.covid.model.CovidModel;
import com.covid.covid.repository.CovidRepository;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.covid.covid")
@AutoConfigurationPackage(basePackageClasses = CovidModel.class)
@EnableJpaRepositories(basePackageClasses = CovidRepository.class)
@EnableScheduling
public class CovidDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(CovidDashboardApplication.class, args);
	}
}