ALTER TABLE keyword_review
    MODIFY taste INT NULL COMMENT '맛 평가 점수',
    MODIFY price INT NULL COMMENT '가격 평가 점수',
    MODIFY food_composition INT NULL COMMENT '음식 구성 평가 점수';

ALTER TABLE user
    MODIFY alarm_type VARCHAR(10) NULL COMMENT '알림 시간 설정';
