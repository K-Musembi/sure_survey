package com.survey_engine.survey_engine;

import org.springframework.boot.SpringApplication;

public class TestSurveyEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(SurveyEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
