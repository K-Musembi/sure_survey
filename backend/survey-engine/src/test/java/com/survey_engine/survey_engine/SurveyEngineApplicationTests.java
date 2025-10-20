package com.survey_engine.survey_engine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {
    "africastalking.api.apiKey=test_api_key",
    "africastalking.api.username=test_username"
})
class SurveyEngineApplicationTests {

	@Test
	void contextLoads() {
	}

}
