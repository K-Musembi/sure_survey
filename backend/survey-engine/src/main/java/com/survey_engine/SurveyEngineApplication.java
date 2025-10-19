package com.survey_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Main entry point for the Survey Engine Spring Boot application.
 * This class bootstraps the application and enables Spring Retry functionality.
 */
@SpringBootApplication
@EnableRetry
public class SurveyEngineApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {

        SpringApplication.run(SurveyEngineApplication.class, args);
	}

}