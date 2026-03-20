package com.survey_engine.ai_analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.ai_analysis.dto.InsightReportRequest;
import com.survey_engine.ai_analysis.dto.InsightReportResult;
import com.survey_engine.common.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Generates structured insight reports and performs sentiment analysis
 * by calling the configured LLM via Spring AI.
 */
@Service
@Slf4j
public class AiInsightService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiInsightService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public InsightReportResult generateInsightReport(InsightReportRequest request) {
        String systemPrompt = """
                You are an expert business intelligence analyst specializing in survey data for African markets.
                You analyze survey results and produce boardroom-ready insights for corporates, NGOs, SACCOs, and insurance companies.
                Your analysis must be:
                - Specific and evidence-based (reference actual scores and patterns in the data)
                - Actionable (recommendations must be implementable)
                - Concise (executive summary max 3 sentences)
                - Structured exactly as the JSON format below — no markdown, no prose outside JSON

                Return ONLY a valid JSON object with this exact structure:
                {
                  "executiveSummary": "string",
                  "keyFindings": [
                    {"type": "STRENGTH|WEAKNESS|RISK|OPPORTUNITY", "text": "string", "area": "string"}
                  ],
                  "recommendations": [
                    {"priority": "HIGH|MEDIUM|LOW", "area": "string", "recommendedAction": "string",
                     "suggestedOwner": "string", "suggestedTimeline": "string"}
                  ],
                  "clusters": [
                    {"name": "string", "size": integer, "description": "string"}
                  ]
                }
                """;

        String userPrompt = buildInsightPrompt(request);

        try {
            String rawResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            assert rawResponse != null;
            String cleanJson = rawResponse.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, InsightReportResult.class);
        } catch (Exception e) {
            log.error("Failed to generate insight report for survey {}: {}", request.surveyId(), e.getMessage(), e);
            throw new ExternalServiceException(
                    "AI_INSIGHT_GENERATION_FAILED",
                    "Failed to generate insight report for survey " + request.surveyId(),
                    e
            );
        }
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.isBlank()) {
            return "NEUTRAL";
        }
        try {
            String response = chatClient.prompt()
                    .system("You are a sentiment classifier. Return ONLY one word: POSITIVE, NEUTRAL, or NEGATIVE.")
                    .user(text)
                    .call()
                    .content();
            assert response != null;
            String result = response.trim().toUpperCase();
            return List.of("POSITIVE", "NEUTRAL", "NEGATIVE").contains(result) ? result : "NEUTRAL";
        } catch (Exception e) {
            log.warn("Sentiment analysis failed, defaulting to NEUTRAL: {}", e.getMessage());
            return "NEUTRAL";
        }
    }

    private String buildInsightPrompt(InsightReportRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Survey: ").append(request.surveyName()).append("\n");
        sb.append("Sector: ").append(request.sector() != null ? request.sector() : "General").append("\n");
        sb.append("Total responses: ").append(request.totalResponses()).append("\n\n");

        sb.append("Question Breakdown:\n");
        for (InsightReportRequest.QuestionSummary q : request.questionSummaries()) {
            sb.append("- [").append(q.questionType()).append("] ");
            if (q.category() != null) sb.append("[Category: ").append(q.category()).append("] ");
            sb.append(q.questionText()).append(": ").append(q.optionBreakdown()).append("\n");
        }

        if (request.openTextResponses() != null && !request.openTextResponses().isEmpty()) {
            sb.append("\nOpen-text samples (first 10):\n");
            request.openTextResponses().stream().limit(10)
                    .forEach(t -> sb.append("  - ").append(t).append("\n"));
        }

        sb.append("\nGenerate the insight report JSON as instructed.");
        return sb.toString();
    }

    private static final List<String> SENTIMENTS = List.of("POSITIVE", "NEUTRAL", "NEGATIVE");
}
