package com.survey_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SurveyEngineApplication {

	public static void main(String[] args) {

        SpringApplication.run(SurveyEngineApplication.class, args);
	}

}
