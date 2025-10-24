package com.waytoearth.exception;

/**
 * 스토리 카드를 찾을 수 없을 때 발생하는 예외
 */
public class StoryCardNotFoundException extends RuntimeException {
    public StoryCardNotFoundException(String message) {
        super(message);
    }

    public StoryCardNotFoundException(Long storyCardId) {
        super("스토리 카드를 찾을 수 없습니다: " + storyCardId);
    }
}
