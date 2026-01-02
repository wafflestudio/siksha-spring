create table if not exists alembic_version
(
    version_num varchar(32) not null
        primary key
);

create table if not exists board
(
    name        varchar(200)                        not null comment '게시판 이름',
    description text                                not null comment '게시판 설명',
    id          int auto_increment
        primary key,
    created_at  timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at  timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    type        int       default 1                 not null comment '게시판 타입(학식,외식)',
    constraint name
        unique (name)
);

create table if not exists image
(
    `key`      varchar(60)                         not null comment '이미지 key',
    category   varchar(10)                         not null comment '이미지 카테고리(POST, PROFILE, REVIEW, ...)',
    user_id    int                                 null comment '이미지를 올린 유저',
    is_deleted tinyint(1)                          not null comment '삭제 여부',
    id         int auto_increment
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint `key`
        unique (`key`)
);

create table if not exists restaurant
(
    id         int auto_increment
        primary key,
    code       varchar(200)                        not null comment '식당 식별자(크롤러에서 사용)',
    name_kr    varchar(200)                        null comment '식당명(한글)',
    name_en    varchar(200)                        null comment '식당명(영어)',
    addr       varchar(200)                        null comment '주소',
    lat        double                              null comment '위도',
    lng        double                              null comment '경도',
    etc        text                                null comment '기타 정보(json 형태로 유연하게 저장)',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint code
        unique (code)
);

create table if not exists menu
(
    id            int auto_increment
        primary key,
    restaurant_id int                                 not null comment '메뉴가 제공되는 식당의 id',
    code          varchar(200)                        not null comment '메뉴 식별자(크롤러에서 사용)',
    date          date                                not null comment '메뉴가 제공되는 날짜',
    type          varchar(10)                         not null comment '메뉴 제공 타입(BR,LU,DN,AL,...)',
    name_kr       varchar(200)                        null comment '메뉴명(한글)',
    name_en       varchar(200)                        null comment '메뉴명(영어)',
    price         int                                 null comment '가격',
    etc           text                                null comment '기타 정보(json 형태로 유연하게 저장)',
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint restaurant_id
        unique (restaurant_id, code, date, type),
    constraint menu_ibfk_1
        foreign key (restaurant_id) references restaurant (id)
            on delete cascade
);

create index menu_code_restaurant_id_index
    on menu (code, restaurant_id);

create index menu_date_index
    on menu (date);

create table if not exists user
(
    id          int auto_increment
        primary key,
    type        varchar(10)                         not null comment '사용자 타입(GOOGLE, APPLE, ...)',
    identity    varchar(200)                        not null comment '사용자 식별자',
    etc         text                                null comment '기타 정보(json 형태로 유연하게 저장)',
    created_at  timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at  timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    nickname    varchar(30)                         not null comment '사용자 닉네임',
    profile_url varchar(100)                        null comment '사용자 프로필 url',
    alarm_type   varchar(10)                         not null comment '알림 시간 설정',
    constraint nickname
        unique (nickname),
    constraint type
        unique (type, identity)
);

create table if not exists menu_like
(
    id         int auto_increment
        primary key,
    user_id    int                                 not null comment '좋아요를 남긴 유저',
    menu_id    int                                 not null comment '좋아요를 남긴 메뉴',
    is_liked   tinyint(1)                          null comment '좋아요 여부',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint menu_id
        unique (menu_id, user_id),
    constraint menu_like_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint menu_like_ibfk_2
        foreign key (menu_id) references menu (id)
            on delete cascade
);

create index menu_like_menu_id_index
    on menu_like (menu_id);

create index menu_like_user_id_index
    on menu_like (user_id);

create table if not exists post
(
    user_id    int                                 not null comment '게시글을 작성한 유저',
    board_id   int                                 not null comment '게시글이 작성된 게시판',
    title      varchar(200)                        not null comment '게시글 제목',
    content    text                                not null comment '게시글 내용',
    available  tinyint(1)                          null comment '표시 여부',
    id         int auto_increment
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    etc        text                                null comment '기타 정보(json 형태로 유연하게 저장)',
    anonymous  tinyint(1)                          not null comment '익명 여부',
    constraint post_ibfk_1
        foreign key (board_id) references board (id)
            on delete cascade,
    constraint post_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
);

create table if not exists comment
(
    user_id    int                                 not null comment '댓글을 작성한 유저',
    post_id    int                                 not null comment '댓글이 작성된 게시글',
    content    text                                not null comment '댓글 내용',
    available  tinyint(1)                          null comment '표시 여부',
    id         int auto_increment
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    anonymous  tinyint(1)                          not null comment '익명 여부',
    constraint comment_ibfk_1
        foreign key (post_id) references post (id)
            on delete cascade,
    constraint comment_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
);

create index comment_post_id_index
    on comment (post_id);

create index comment_user_id_index
    on comment (user_id);

create table if not exists comment_like
(
    user_id    int                                 not null comment '좋아요를 남긴 유저',
    comment_id int                                 not null comment '좋아요를 남긴 댓글',
    is_liked   tinyint(1)                          null comment '좋아요 여부',
    id         int auto_increment
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint comment_id
        unique (comment_id, user_id),
    constraint comment_like_ibfk_1
        foreign key (comment_id) references comment (id)
            on delete cascade,
    constraint comment_like_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
);

create index comment_like_comment_id_index
    on comment_like (comment_id);

create index comment_like_user_id_index
    on comment_like (user_id);

create table if not exists comment_report
(
    comment_id    int                                 not null comment '댓글 ID',
    reason        varchar(200)                        not null comment '신고 사유',
    reporting_uid int                                 null comment '신고자 ID',
    reported_uid  int                                 not null comment '신고 당한 유저 ID',
    id            int auto_increment
        primary key,
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint comment_id
        unique (comment_id, reporting_uid),
    constraint comment_report_ibfk_1
        foreign key (reported_uid) references user (id)
            on delete cascade,
    constraint comment_report_ibfk_2
        foreign key (reporting_uid) references user (id)
            on delete cascade,
    constraint fk_comment_report_comment_id
        foreign key (comment_id) references comment (id)
            on delete cascade
);

create index comment_report_comment_id_index
    on comment_report (comment_id);

create index comment_report_reported_uid_index
    on comment_report (reported_uid);

create index comment_report_reporting_uid_index
    on comment_report (reporting_uid);

create index post_board_id_index
    on post (board_id);

create index post_user_id_index
    on post (user_id);

create table if not exists post_like
(
    user_id    int                                 not null comment '좋아요를 남긴 유저',
    post_id    int                                 not null comment '좋아요를 남긴 게시글',
    is_liked   tinyint(1)                          null comment '좋아요 여부',
    id         int auto_increment
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint post_id
        unique (post_id, user_id),
    constraint post_like_ibfk_1
        foreign key (post_id) references post (id)
            on delete cascade,
    constraint post_like_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
);

create index post_like_post_id_index
    on post_like (post_id);

create index post_like_user_id_index
    on post_like (user_id);

create table if not exists post_report
(
    post_id       int                                 not null comment '게시글 ID',
    reason        varchar(200)                        not null comment '신고 사유',
    reporting_uid int                                 null comment '신고자 ID',
    reported_uid  int                                 not null comment '신고 당한 유저 ID',
    id            int auto_increment
        primary key,
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint post_id
        unique (post_id, reporting_uid),
    constraint fk_post_report_post_id
        foreign key (post_id) references post (id)
            on delete cascade,
    constraint post_report_ibfk_1
        foreign key (reported_uid) references user (id)
            on delete cascade,
    constraint post_report_ibfk_2
        foreign key (reporting_uid) references user (id)
            on delete cascade
);

create index post_report_post_id_index
    on post_report (post_id);

create index post_report_reported_uid_index
    on post_report (reported_uid);

create index post_report_reporting_uid_index
    on post_report (reporting_uid);

create table if not exists review
(
    id         int auto_increment
        primary key,
    user_id    int                                 not null comment '리뷰를 작성한 사용자의 id',
    menu_id    int                                 not null comment '리뷰의 대상 메뉴 id',
    score      int                                 not null comment '메뉴에 대한 평점',
    comment    text                                null comment '메뉴에 대한 리뷰',
    etc        text                                null comment '기타 정보(json 형태로 유연하게 저장)',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint menu_id
        unique (menu_id, user_id),
    constraint review_ibfk_1
        foreign key (menu_id) references menu (id)
            on delete cascade,
    constraint review_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
);

create index review_user_id_index
    on review (user_id);

create table if not exists keyword_review (
    review_id           int
        primary key,
    taste               int                          not null comment '맛 평가 점수',
    price               int                          not null comment '가격 평가 점수',
    food_composition    int                          not null comment '음식 구성 평가 점수',
    restaurant_id       int                          not null comment '리뷰한 메뉴의 레스토랑 id',
    menu_code           varchar(200)                 not null comment '리뷰한 메뉴의 식별자',
    menu_date           date                         not null comment '리뷰한 메뉴의 날짜',
    menu_type           varchar(10)                  not null comment '리뷰한 메뉴의 타입',
    constraint keyword_review_ibfk_1
        foreign key (review_id) references review (id)
        on delete cascade,
    constraint keyword_review_ibfk_2
        foreign key (restaurant_id, menu_code, menu_date, menu_type) references menu (restaurant_id, code, date, type)
        on delete cascade
);

create index keyword_review_menu_key_index
    on keyword_review (restaurant_id, menu_code);

create table if not exists review_like (
    id         int auto_increment
        primary key,
    user_id    int                                 not null comment '리뷰를 좋아한 사용자의 id',
    review_id  int                                 not null comment '사용자가 좋아한 리뷰 id',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint review_id
        unique (review_id, user_id),
    constraint review_like_ibfk_1
        foreign key (user_id) references user (id)
        on delete cascade,
    constraint review_like_ibfk_2
        foreign key (review_id) references review (id)
        on delete cascade
);

create index review_like_review_id_index
    on review_like (review_id);


create table if not exists user_device (
   id         int auto_increment
   primary key,
   user_id    int                                 not null comment '사용자 id',
   fcm_token  varchar(255)                        not null comment 'fcm 토큰',
   created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',

    constraint user_id_fcm_token
    unique (user_id, fcm_token)
);

create index user_device_user_id_index
    on user_device (user_id);

create table if not exists menu_alarm
(
    id         int auto_increment
        primary key,
    user_id    int                                 not null comment '알림 유저',
    menu_id    int                                 not null comment '알림 메뉴',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    constraint menu_id
        unique (menu_id, user_id),
    constraint menu_alarm_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint menu_alarm_ibfk_2
        foreign key (menu_id) references menu (id)
            on delete cascade
);

CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  ,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME DATETIME(6) NOT NULL,
                                      START_TIME DATETIME(6) DEFAULT NULL ,
                                      END_TIME DATETIME(6) DEFAULT NULL ,
                                      STATUS VARCHAR(10) ,
                                      EXIT_CODE VARCHAR(2500) ,
                                      EXIT_MESSAGE VARCHAR(2500) ,
                                      LAST_UPDATED DATETIME(6),
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       CREATE_TIME DATETIME(6) NOT NULL,
                                       START_TIME DATETIME(6) DEFAULT NULL ,
                                       END_TIME DATETIME(6) DEFAULT NULL ,
                                       STATUS VARCHAR(10) ,
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED DATETIME(6),
                                       constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                           references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                  references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
                                          ID BIGINT NOT NULL,
                                          UNIQUE_KEY CHAR(1) NOT NULL,
                                          constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
                                         ID BIGINT NOT NULL,
                                         UNIQUE_KEY CHAR(1) NOT NULL,
                                         constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ (
                               ID BIGINT NOT NULL,
                               UNIQUE_KEY CHAR(1) NOT NULL,
                               constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);
