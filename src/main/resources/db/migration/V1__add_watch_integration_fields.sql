-- 갤럭시 워치 연동 및 차트 데이터를 위한 필드 추가
-- 작성일: 2025-01-30

-- 1. running_record 테이블에 심박수 필드 추가
ALTER TABLE running_record
ADD COLUMN average_heart_rate INT NULL COMMENT '평균 심박수(bpm)',
ADD COLUMN max_heart_rate INT NULL COMMENT '최대 심박수(bpm)';

-- 2. running_route 테이블에 차트용 상세 필드 추가
ALTER TABLE running_route
ADD COLUMN timestamp_seconds INT NULL COMMENT '러닝 시작 후 경과 시간(초)',
ADD COLUMN heart_rate INT NULL COMMENT '이 지점에서의 심박수(bpm)',
ADD COLUMN pace_seconds INT NULL COMMENT '이 구간의 페이스(초/km)',
ADD COLUMN altitude DOUBLE NULL COMMENT '고도(미터)',
ADD COLUMN accuracy DOUBLE NULL COMMENT 'GPS 정확도(미터)',
ADD COLUMN cumulative_distance_meters INT NULL COMMENT '이 지점까지의 누적 거리(미터)';

-- 3. running_route 테이블에 시간대별 조회 최적화를 위한 인덱스 추가
CREATE INDEX idx_route_timestamp
ON running_route(running_record_id, timestamp_seconds);
