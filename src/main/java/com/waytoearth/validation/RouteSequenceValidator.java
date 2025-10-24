package com.waytoearth.validation;

import com.waytoearth.entity.journey.JourneyRouteEntity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 경로 sequence의 연속성을 검증하는 Validator
 */
public class RouteSequenceValidator implements ConstraintValidator<ValidRouteSequence, List<JourneyRouteEntity>> {

    @Override
    public void initialize(ValidRouteSequence constraintAnnotation) {
        // 초기화 로직 없음
    }

    @Override
    public boolean isValid(List<JourneyRouteEntity> routes, ConstraintValidatorContext context) {
        if (routes == null || routes.isEmpty()) {
            return true; // 빈 리스트는 유효한 것으로 간주
        }

        // sequence 중복 검사
        Set<Integer> sequences = routes.stream()
                .map(JourneyRouteEntity::getSequence)
                .collect(Collectors.toSet());

        if (sequences.size() != routes.size()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("경로 sequence에 중복된 값이 있습니다")
                    .addConstraintViolation();
            return false;
        }

        // 1부터 시작하는 연속성 검사
        Set<Integer> expectedSequences = IntStream.rangeClosed(1, routes.size())
                .boxed()
                .collect(Collectors.toSet());

        if (!sequences.equals(expectedSequences)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("경로 sequence가 연속적이지 않습니다. 기대값: %s, 실제값: %s",
                            expectedSequences, sequences))
                    .addConstraintViolation();
            return false;
        }

        // 중복 좌표 검증 (소수점 6자리까지 동일한 경우)
        for (int i = 1; i < routes.size(); i++) {
            JourneyRouteEntity prev = routes.get(i - 1);
            JourneyRouteEntity current = routes.get(i);

            if (Math.abs(prev.getLatitude() - current.getLatitude()) < 0.000001 &&
                Math.abs(prev.getLongitude() - current.getLongitude()) < 0.000001) {

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("sequence %d와 %d에서 중복 좌표가 감지되었습니다: (%.6f, %.6f)",
                                prev.getSequence(), current.getSequence(),
                                current.getLatitude(), current.getLongitude()))
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}