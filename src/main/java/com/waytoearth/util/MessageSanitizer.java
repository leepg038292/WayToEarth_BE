package com.waytoearth.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@Component
public class MessageSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile("(?i)(javascript:|data:|vbscript:|onload|onerror|onclick)");

    /**
     * 메시지 내용을 안전하게 정제합니다.
     * XSS 공격을 방지하고 HTML 태그를 이스케이프합니다.
     */
    public String sanitizeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        // 1. 앞뒤 공백 제거
        String sanitized = message.trim();

        // 2. 스크립트 태그 완전 제거
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // 3. 악성 JavaScript 패턴 제거
        sanitized = MALICIOUS_PATTERN.matcher(sanitized).replaceAll("");

        // 4. HTML 태그 이스케이프
        sanitized = HtmlUtils.htmlEscape(sanitized);

        // 5. 연속된 공백을 하나로 정리
        sanitized = sanitized.replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * 메시지 길이 검증
     */
    public boolean isValidLength(String message) {
        return message != null && message.length() >= 1 && message.length() <= 1000;
    }

    /**
     * 스팸성 메시지 패턴 검사 (기본적인 패턴만)
     */
    public boolean isSpamMessage(String message) {
        if (message == null) return false;

        // 동일 문자 반복 (10개 이상)
        if (message.matches(".*(..)\\1{10,}.*")) {
            return true;
        }

        // URL 패턴이 너무 많은 경우 (5개 이상)
        long urlCount = message.toLowerCase()
                .chars()
                .mapToObj(c -> String.valueOf((char) c))
                .reduce("", String::concat)
                .split("http[s]?://|www\\.")
                .length - 1;

        return urlCount >= 5;
    }
}