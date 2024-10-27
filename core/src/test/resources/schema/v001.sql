DROP TABLE IF EXISTS `restaurant`;

CREATE TABLE `restaurant` (
    `id` int NOT NULL AUTO_INCREMENT,
    `code` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '식당 식별자(크롤러에서 사용)',
    `name_kr` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '식당명(한글)',
    `name_en` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '식당명(영어)',
    `addr` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '주소',
    `lat` double DEFAULT NULL COMMENT '위도',
    `lng` double DEFAULT NULL COMMENT '경도',
    `etc` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '기타 정보(json 형태로 유연하게 저장)',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    PRIMARY KEY (`id`),
    UNIQUE KEY `code` (`code`)
)
