package com.survey_engine.ai_analysis.service;

import com.survey_engine.ai_analysis.dto.AiResponse;
import com.survey_engine.ai_analysis.dto.SurveyAnalysisRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Agentic service responsible for analyzing survey data.
 * This service uses an AI Agent pattern implemented via Spring AI.
 * The {@link ChatClient} is autoconfigured with an API key and base URL from the application properties.
 * The agent is initialized with a system prompt defining its role as a data analyst.
 * It is registered with a "Tool" (Function Calling) named {@code fetchSurveyResponses}.
 * Spring AI automatically executes the Java method associated with the tool, retrieves the
 * survey responses from the database, and feeds them back to the LLM.
 * The LLM then processes this data to generate the final insight or summary.
 */
@Service
public class AiAnalysisAgent {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisAgent.class);
    private final ChatClient chatClient;

    public AiAnalysisAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are an expert data analyst. You analyze survey responses to provide insights. " +
                        "You have access to a tool 'fetchSurveyResponses' which you MUST use to get the data before answering. " +
                        "If the user asks for analysis of a survey, call the tool with the survey ID.")
                .build();
    }

    /**
     * Performs analysis on a specific survey.
     *
     * @param request The request containing the survey ID and the user's specific query (e.g., "Summarize sentiment").
     * @return An {@link AiResponse} containing the AI-generated analysis.
     */
    public AiResponse analyzeSurvey(SurveyAnalysisRequest request) {
        String userQuery = request.query();
        if (userQuery == null || userQuery.isBlank()) {
            userQuery = "Analyze the sentiment and key themes of the responses for survey ID " + request.surveyId();
        } else {
            // Ensure the survey ID is part of the context if the user didn't explicitly mention it in the text
            userQuery = "For Survey ID " + request.surveyId() + ": " + userQuery;
        }

        logger.info("Agent analyzing survey {}: {}", request.surveyId(), userQuery);

        String response = chatClient.prompt()
                .user(userQuery)
                .functions("fetchSurveyResponses") // Register the tool
                .call()
                .content();

        return new AiResponse(response);
    }
}
