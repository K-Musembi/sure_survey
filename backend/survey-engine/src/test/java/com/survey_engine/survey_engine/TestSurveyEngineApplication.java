package com.survey_engine.survey_engine;

import com.survey_engine.SurveyEngineApplication;
import org.springframework.boot.SpringApplication;

public class TestSurveyEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(SurveyEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
