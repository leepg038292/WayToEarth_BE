package com.waytoearth.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * OpenAI API 연동 서비스
 * - Chat Completion API를 통한 AI 분석 제공
 */
@Slf4j
@Service
public class OpenAIService {

    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private static final int DEFAULT_MAX_TOKENS = 500;
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final OpenAiService openAiService;
    private final String model;

    public OpenAIService(@Value("${openai.api-key}") String apiKey,
                         @Value("${openai.model:" + DEFAULT_MODEL + "}") String model) {
        this.openAiService = new OpenAiService(apiKey, DEFAULT_TIMEOUT);
        this.model = model;
        log.info("OpenAI Service initialized with model: {}", model);
    }

    /**
     * Chat Completion API 호출
     *
     * @param systemPrompt 시스템 프롬프트 (AI 역할 정의)
     * @param userPrompt   사용자 프롬프트 (실제 요청 내용)
     * @return ChatCompletionResult (응답 + 토큰 사용량 등 메타데이터)
     */
    public ChatCompletionResult createChatCompletion(String systemPrompt, String userPrompt) {
        log.debug("Creating chat completion - System: {}, User: {}", systemPrompt, userPrompt);

        List<ChatMessage> messages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .temperature(DEFAULT_TEMPERATURE)
                .build();

        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            log.info("Chat completion success - Tokens used: {}", result.getUsage().getTotalTokens());
            return result;
        } catch (Exception e) {
            log.error("Failed to create chat completion", e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 응답 텍스트만 간단히 추출
     */
    public String getCompletionText(ChatCompletionResult result) {
        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
            return "";
        }
        return result.getChoices().get(0).getMessage().getContent();
    }
}
