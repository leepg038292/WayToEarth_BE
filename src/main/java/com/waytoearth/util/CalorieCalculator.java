package com.waytoearth.util;

/**
 * 칼로리 계산 유틸리티 (METs 기반)
 *
 * <p><strong>⚠️ 프론트엔드/워치 개발자 필독:</strong></p>
 * <p>이 클래스에 정의된 계산 공식을 <strong>동일하게</strong> 프론트엔드와 워치에서 구현하세요.</p>
 * <p>계산 로직의 일관성을 유지하기 위해 아래 공식을 그대로 사용해주세요.</p>
 *
 * <h3>📐 칼로리 계산 공식 (METs 방식)</h3>
 * <pre>
 * 칼로리(kcal) = 체중(kg) × METs × 시간(h) × 1.05
 *
 * METs 값 (속도 기준):
 * - 걷기 (< 6 km/h):      METs 3.5
 * - 조깅 (6 ~ 8 km/h):    METs 7.0
 * - 러닝 (8 ~ 10 km/h):   METs 9.0
 * - 빠른 러닝 (≥ 10 km/h): METs 11.0
 * </pre>
 *
 * <h3>🔍 계산 예시</h3>
 * <pre>
 * 예1) 70kg, 5km, 30분 (10 km/h) 러닝
 *   → 속도 = 5km / 0.5h = 10 km/h → METs 9.0
 *   → 칼로리 = 70 × 9.0 × 0.5 × 1.05 = 330.75 kcal
 *
 * 예2) 60kg, 1km, 14분 (4.3 km/h) 걷기
 *   → 속도 = 1km / 0.233h = 4.3 km/h → METs 3.5
 *   → 칼로리 = 60 × 3.5 × 0.233 × 1.05 = 51.4 kcal
 * </pre>
 *
 * <h3>📱 프론트엔드 구현 가이드 (TypeScript/JavaScript)</h3>
 * <pre>
 * function calculateCalories(distanceKm: number, durationSeconds: number, weightKg: number): number {
 *   if (distanceKm <= 0 || durationSeconds <= 0 || weightKg <= 0) {
 *     return 0;
 *   }
 *
 *   const durationHours = durationSeconds / 3600.0;
 *   const speedKmh = distanceKm / durationHours;
 *
 *   let mets: number;
 *   if (speedKmh < 6.0) {
 *     mets = 3.5;  // 걷기
 *   } else if (speedKmh < 8.0) {
 *     mets = 7.0;  // 조깅
 *   } else if (speedKmh < 10.0) {
 *     mets = 9.0;  // 러닝
 *   } else {
 *     mets = 11.0; // 빠른 러닝
 *   }
 *
 *   const calories = weightKg * mets * durationHours * 1.05;
 *   return Math.round(calories);
 * }
 * </pre>
 *
 * <h3>⌚ 워치 구현 가이드 (Kotlin/Android)</h3>
 * <pre>
 * fun calculateCalories(distanceKm: Double, durationSeconds: Int, weightKg: Int): Int {
 *   if (distanceKm <= 0 || durationSeconds <= 0 || weightKg <= 0) {
 *     return 0
 *   }
 *
 *   val durationHours = durationSeconds / 3600.0
 *   val speedKmh = distanceKm / durationHours
 *
 *   val mets = when {
 *     speedKmh < 6.0 -> 3.5  // 걷기
 *     speedKmh < 8.0 -> 7.0  // 조깅
 *     speedKmh < 10.0 -> 9.0  // 러닝
 *     else -> 11.0           // 빠른 러닝
 *   }
 *
 *   val calories = weightKg * mets * durationHours * 1.05
 *   return calories.roundToInt()
 * }
 * </pre>
 *
 * @author WayToEarth Team
 * @since 2025-01-09
 */
public class CalorieCalculator {

    /**
     * 칼로리 계산 (METs 기반)
     *
     * @param distanceKm      거리(km)
     * @param durationSeconds 시간(초)
     * @param weightKg        체중(kg)
     * @return 소모 칼로리(kcal), 반올림된 정수
     */
    public static int calculate(double distanceKm, int durationSeconds, int weightKg) {
        // 유효성 검증
        if (distanceKm <= 0 || durationSeconds <= 0 || weightKg <= 0) {
            return 0;
        }

        // 시간을 시간(hour) 단위로 변환
        double durationHours = durationSeconds / 3600.0;

        // 속도(km/h) 계산
        double speedKmh = distanceKm / durationHours;

        // 속도에 따른 METs 값 결정
        double mets;
        if (speedKmh < 6.0) {
            mets = 3.5;  // 걷기
        } else if (speedKmh < 8.0) {
            mets = 7.0;  // 조깅
        } else if (speedKmh < 10.0) {
            mets = 9.0;  // 러닝
        } else {
            mets = 11.0; // 빠른 러닝
        }

        // 칼로리 계산: 체중(kg) × METs × 시간(h) × 1.05
        double calories = weightKg * mets * durationHours * 1.05;

        // 반올림하여 정수로 반환
        return (int) Math.round(calories);
    }

    /**
     * 속도(km/h) 계산 헬퍼 메서드
     *
     * @param distanceKm      거리(km)
     * @param durationSeconds 시간(초)
     * @return 속도(km/h)
     */
    public static double calculateSpeed(double distanceKm, int durationSeconds) {
        if (distanceKm <= 0 || durationSeconds <= 0) {
            return 0.0;
        }
        double durationHours = durationSeconds / 3600.0;
        return distanceKm / durationHours;
    }

    /**
     * METs 값 계산 헬퍼 메서드
     *
     * @param speedKmh 속도(km/h)
     * @return METs 값
     */
    public static double getMets(double speedKmh) {
        if (speedKmh < 6.0) {
            return 3.5;  // 걷기
        } else if (speedKmh < 8.0) {
            return 7.0;  // 조깅
        } else if (speedKmh < 10.0) {
            return 9.0;  // 러닝
        } else {
            return 11.0; // 빠른 러닝
        }
    }
}
