package com.waytoearth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 여정 경로의 sequence 연속성을 검증하는 어노테이션
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RouteSequenceValidator.class)
public @interface ValidRouteSequence {

    String message() default "경로 sequence가 유효하지 않습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}