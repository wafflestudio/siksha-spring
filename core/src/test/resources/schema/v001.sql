SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS board CASCADE;
DROP TABLE if EXISTS image CASCADE;
DROP TABLE if EXISTS restaurant CASCADE;
DROP TABLE if EXISTS menu CASCADE;
DROP TABLE if EXISTS user CASCADE;
DROP TABLE if EXISTS menu_like CASCADE;
DROP TABLE if EXISTS post CASCADE;
DROP TABLE if EXISTS post_like CASCADE;
DROP TABLE if EXISTS post_report CASCADE;
DROP TABLE if EXISTS comment CASCADE;
DROP TABLE if EXISTS comment_like CASCADE;
DROP TABLE if EXISTS comment_report CASCADE;
DROP TABLE if EXISTS review CASCADE;
DROP TABLE if EXISTS version CASCADE;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE board
(
    name        varchar(200)                        NOT NULL COMMENT '게시판 이름',
    description mediumtext                          NOT NULL COMMENT '게시판 설명',
    id          int AUTO_INCREMENT
        PRIMARY KEY,
    created_at  timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at  timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    type        int       DEFAULT 1                 NOT NULL COMMENT '게시판 타입(학식,외식)',
    CONSTRAINT name
        UNIQUE (name)
)
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE image
(
    `key`      varchar(60)                         NOT NULL COMMENT '이미지 key',
    category   varchar(10)                         NOT NULL COMMENT '이미지 카테고리(POST, PROFILE, REVIEW, ...)',
    user_id    int                                 NULL COMMENT '이미지를 올린 유저',
    is_deleted tinyint(1)                          NOT NULL COMMENT '삭제 여부',
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT `key`
        UNIQUE (`key`)
);

CREATE TABLE restaurant
(
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    code       varchar(200)                        NOT NULL COMMENT '식당 식별자(크롤러에서 사용)',
    name_kr    varchar(200)                        NULL COMMENT '식당명(한글)',
    name_en    varchar(200)                        NULL COMMENT '식당명(영어)',
    addr       varchar(200)                        NULL COMMENT '주소',
    lat        double                              NULL COMMENT '위도',
    lng        double                              NULL COMMENT '경도',
    etc        mediumtext                          NULL COMMENT '기타 정보(json 형태로 유연하게 저장)',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT code
        UNIQUE (code)
)
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE menu
(
    id            int AUTO_INCREMENT
        PRIMARY KEY,
    restaurant_id int                                 NOT NULL COMMENT '메뉴가 제공되는 식당의 id',
    code          varchar(200)                        NOT NULL COMMENT '메뉴 식별자(크롤러에서 사용)',
    date          date                                NOT NULL COMMENT '메뉴가 제공되는 날짜',
    type          varchar(10)                         NOT NULL COMMENT '메뉴 제공 타입(BR,LU,DN,AL,...)',
    name_kr       varchar(200)                        NULL COMMENT '메뉴명(한글)',
    name_en       varchar(200)                        NULL COMMENT '메뉴명(영어)',
    price         int                                 NULL COMMENT '가격',
    etc           mediumtext                          NULL COMMENT '기타 정보(json 형태로 유연하게 저장)',
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT restaurant_id
        UNIQUE (restaurant_id, code, date, type),
    CONSTRAINT menu_ibfk_1
        FOREIGN KEY (restaurant_id) REFERENCES restaurant (id)
            ON DELETE CASCADE
)
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX menu_code_restaurant_id_index
    ON menu (code, restaurant_id);

CREATE INDEX menu_date_index
    ON menu (date);

CREATE TABLE user
(
    id          int AUTO_INCREMENT
        PRIMARY KEY,
    type        varchar(10)                         NOT NULL COMMENT '사용자 타입(GOOGLE, APPLE, ...)',
    identity    varchar(200)                        NOT NULL COMMENT '사용자 식별자',
    etc         mediumtext                          NULL COMMENT '기타 정보(json 형태로 유연하게 저장)',
    created_at  timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at  timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    nickname    varchar(30)                         NOT NULL COMMENT '사용자 닉네임',
    profile_url varchar(100)                        NULL COMMENT '사용자 프로필 url',
    CONSTRAINT nickname
        UNIQUE (nickname),
    CONSTRAINT type
        UNIQUE (type, identity)
)
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE menu_like
(
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    user_id    int                                 NOT NULL COMMENT '좋아요를 남긴 유저',
    menu_id    int                                 NOT NULL COMMENT '좋아요를 남긴 메뉴',
    is_liked   tinyint(1)                          NULL COMMENT '좋아요 여부',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT menu_id
        UNIQUE (menu_id, user_id),
    CONSTRAINT menu_like_ibfk_1
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT menu_like_ibfk_2
        FOREIGN KEY (menu_id) REFERENCES menu (id)
            ON DELETE CASCADE
)
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX menu_like_menu_id_index
    ON menu_like (menu_id);

CREATE INDEX menu_like_user_id_index
    ON menu_like (user_id);

CREATE TABLE post
(
    user_id    int                                 NOT NULL COMMENT '게시글을 작성한 유저',
    board_id   int                                 NOT NULL COMMENT '게시글이 작성된 게시판',
    title      varchar(200)                        NOT NULL COMMENT '게시글 제목',
    content    mediumtext                          NOT NULL COMMENT '게시글 내용',
    available  tinyint(1)                          NULL COMMENT '표시 여부',
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    etc        mediumtext                          NULL COMMENT '기타 정보(json 형태로 유연하게 저장)',
    anonymous  tinyint(1)                          NOT NULL COMMENT '익명 여부',
    CONSTRAINT post_ibfk_1
        FOREIGN KEY (board_id) REFERENCES board (id)
            ON DELETE CASCADE,
    CONSTRAINT post_ibfk_2
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE
)
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE comment
(
    user_id    int                                 NOT NULL COMMENT '댓글을 작성한 유저',
    post_id    int                                 NOT NULL COMMENT '댓글이 작성된 게시글',
    content    mediumtext                          NOT NULL COMMENT '댓글 내용',
    available  tinyint(1)                          NULL COMMENT '표시 여부',
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    anonymous  tinyint(1)                          NOT NULL COMMENT '익명 여부',
    CONSTRAINT comment_ibfk_1
        FOREIGN KEY (post_id) REFERENCES post (id)
            ON DELETE CASCADE,
    CONSTRAINT comment_ibfk_2
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE
)
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX comment_post_id_index
    ON comment (post_id);

CREATE INDEX comment_user_id_index
    ON comment (user_id);

CREATE TABLE comment_like
(
    user_id    int                                 NOT NULL COMMENT '좋아요를 남긴 유저',
    comment_id int                                 NOT NULL COMMENT '좋아요를 남긴 댓글',
    is_liked   tinyint(1)                          NULL COMMENT '좋아요 여부',
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT comment_id
        UNIQUE (comment_id, user_id),
    CONSTRAINT comment_like_ibfk_1
        FOREIGN KEY (comment_id) REFERENCES comment (id)
            ON DELETE CASCADE,
    CONSTRAINT comment_like_ibfk_2
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE
);

CREATE INDEX comment_like_comment_id_index
    ON comment_like (comment_id);

CREATE INDEX comment_like_user_id_index
    ON comment_like (user_id);

CREATE TABLE comment_report
(
    comment_id    int                                 NOT NULL COMMENT '댓글 ID',
    reason        varchar(200)                        NOT NULL COMMENT '신고 사유',
    reporting_uid int                                 NULL COMMENT '신고자 ID',
    reported_uid  int                                 NOT NULL COMMENT '신고 당한 유저 ID',
    id            int AUTO_INCREMENT
        PRIMARY KEY,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT comment_id
        UNIQUE (comment_id, reporting_uid),
    CONSTRAINT comment_report_ibfk_1
        FOREIGN KEY (reported_uid) REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT comment_report_ibfk_2
        FOREIGN KEY (reporting_uid) REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_comment_report_comment_id
        FOREIGN KEY (comment_id) REFERENCES comment (id)
            ON DELETE CASCADE
);

CREATE INDEX comment_report_comment_id_index
    ON comment_report (comment_id);

CREATE INDEX comment_report_reported_uid_index
    ON comment_report (reported_uid);

CREATE INDEX comment_report_reporting_uid_index
    ON comment_report (reporting_uid);

CREATE INDEX post_board_id_index
    ON post (board_id);

CREATE INDEX post_user_id_index
    ON post (user_id);

CREATE TABLE post_like
(
    user_id    int                                 NOT NULL COMMENT '좋아요를 남긴 유저',
    post_id    int                                 NOT NULL COMMENT '좋아요를 남긴 게시글',
    is_liked   tinyint(1)                          NULL COMMENT '좋아요 여부',
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT post_id
        UNIQUE (post_id, user_id),
    CONSTRAINT post_like_ibfk_1
        FOREIGN KEY (post_id) REFERENCES post (id)
            ON DELETE CASCADE,
    CONSTRAINT post_like_ibfk_2
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE
);

CREATE INDEX post_like_post_id_index
    ON post_like (post_id);

CREATE INDEX post_like_user_id_index
    ON post_like (user_id);

CREATE TABLE post_report
(
    post_id       int                                 NOT NULL COMMENT '게시글 ID',
    reason        varchar(200)                        NOT NULL COMMENT '신고 사유',
    reporting_uid int                                 NULL COMMENT '신고자 ID',
    reported_uid  int                                 NOT NULL COMMENT '신고 당한 유저 ID',
    id            int AUTO_INCREMENT
        PRIMARY KEY,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at    timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT post_id
        UNIQUE (post_id, reporting_uid),
    CONSTRAINT fk_post_report_post_id
        FOREIGN KEY (post_id) REFERENCES post (id)
            ON DELETE CASCADE,
    CONSTRAINT post_report_ibfk_1
        FOREIGN KEY (reported_uid) REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT post_report_ibfk_2
        FOREIGN KEY (reporting_uid) REFERENCES user (id)
            ON DELETE CASCADE
);

CREATE INDEX post_report_post_id_index
    ON post_report (post_id);

CREATE INDEX post_report_reported_uid_index
    ON post_report (reported_uid);

CREATE INDEX post_report_reporting_uid_index
    ON post_report (reporting_uid);

CREATE TABLE review
(
    id         int AUTO_INCREMENT
        PRIMARY KEY,
    menu_id    int                                 NOT NULL COMMENT '리뷰의 대상 메뉴 id',
    user_id    int                                 NOT NULL COMMENT '리뷰를 작성한 사용자의 id',
    score      int                                 NOT NULL COMMENT '메뉴에 대한 평점',
    comment    mediumtext                          NULL COMMENT '메뉴에 대한 리뷰',
    etc        mediumtext                          NULL COMMENT '기타 정보(json 형태로 유연하게 저장)',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간',
    CONSTRAINT menu_id
        UNIQUE (menu_id, user_id),
    CONSTRAINT review_ibfk_1
        FOREIGN KEY (user_id) REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT review_ibfk_2
        FOREIGN KEY (menu_id) REFERENCES menu (id)
            ON DELETE CASCADE
)
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX review_user_id_index
    ON review (user_id);

CREATE TABLE version
(
    version         varchar(20)                         NOT NULL COMMENT '버전',
    minimum_version varchar(20)                         NOT NULL COMMENT '최소 버전',
    client_type     varchar(10)                         NOT NULL COMMENT '클라이언트 타입(AND, IOS, WEB)',
    id              int AUTO_INCREMENT
        PRIMARY KEY,
    created_at      timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시간',
    updated_at      timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '변경 시간'
)
    COLLATE = utf8mb4_unicode_ci;
