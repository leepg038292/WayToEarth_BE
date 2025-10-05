package com.waytoearth.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.waytoearth.exception.OpenAIServiceException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * OpenAI API 연동 서비스
 * - Chat Completion API를 통한 AI 분석 제공
 */
@Slf4j
@Service
public class OpenAIService {

    private final OpenAiService openAiService;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public OpenAIService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.max-tokens}") int maxTokens,
            @Value("${openai.temperature}") double temperature,
            @Value("${openai.timeout-seconds}") int timeoutSeconds) {

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OpenAI API 키가 설정되지 않았습니다. application.yml 또는 환경 변수를 확인하세요.");
        }

        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    @PostConstruct
    public void init() {
        log.info("OpenAI Service initialized - Model: {}, MaxTokens: {}, Temperature: {}",
                model, maxTokens, temperature);
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
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();

        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            log.info("Chat completion success - Model: {}, Tokens used: {}, Prompt: {}, Completion: {}",
                    result.getModel(),
                    result.getUsage().getTotalTokens(),
                    result.getUsage().getPromptTokens(),
                    result.getUsage().getCompletionTokens());
            return result;
        } catch (com.theokanning.openai.OpenAiHttpException e) {
            log.error("OpenAI HTTP error - Status: {}, Code: {}", e.statusCode, e.code, e);
            throw new OpenAIServiceException("OpenAI API 호출 실패 (HTTP " + e.statusCode + "): " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during OpenAI API call", e);
            throw new OpenAIServiceException("OpenAI API 호출 중 예기치 않은 오류 발생: " + e.getMessage(), e);
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
