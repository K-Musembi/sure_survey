package com.survey_engine.ai_analysis.service;

import com.survey_engine.ai_analysis.dto.AiGeneratedQuestion;
import com.survey_engine.ai_analysis.dto.SurveyGenerationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for generating survey questions using Generative AI.
 * This service leverages Spring AI to interact with an LLM (Large Language Model).
 * It uses the {@link ChatClient}, which is autoconfigured by Spring Boot based on properties
 * defined in {@code application.yaml} (e.g., {@code spring.ai.openai.api-key}, {@code spring.ai.openai.base-url}).
 */
@Service
public class AiGenerationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiGenerationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a list of survey questions based on the user's request criteria.
     * It constructs a structured prompt instructing the LLM to act as an expert survey designer
     * and return the output as a strict JSON array. The JSON is then parsed into a list of
     * {@link AiGeneratedQuestion} objects.
     *
     * @param request The request DTO containing the topic, sector, type, and number of questions.
     * @return A list of generated questions.
     * @throws RuntimeException if the AI response cannot be parsed into the expected JSON format.
     */
    public List<AiGeneratedQuestion> generateSurveyQuestions(SurveyGenerationRequest request) {
        String userPrompt = String.format(
                "Generate %d survey questions for a '%s' survey in the '%s' sector regarding '%s'.",
                request.questionCount() != null ? request.questionCount() : 5,
                request.type(),
                request.sector() != null ? request.sector() : "General",
                request.topic()
        );

        String systemPrompt = """
                You are an expert survey designer.
                Generate a list of questions based on the user's request.
                
                You must return the response strictly as a JSON array of objects matching this structure:
                [
                  {
                    "questionText": "string",
                    "questionType": "FREE_TEXT | MULTIPLE_CHOICE_SINGLE | MULTIPLE_CHOICE_MULTI | NPS_SCALE | RATING_STAR | RATING_LINEAR",
                    "options": "string (JSON array of options if applicable, else null)",
                    "position": integer
                  }
                ]
                
                Do not include markdown formatting like ```json ... ```. Just the raw JSON array.
                Ensure question types are valid and match the provided list.
                """;

        String jsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        try {
            // Clean up potential markdown if the model disobeys
            assert jsonResponse != null;
            String cleanJson = jsonResponse.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, new TypeReference<List<AiGeneratedQuestion>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}