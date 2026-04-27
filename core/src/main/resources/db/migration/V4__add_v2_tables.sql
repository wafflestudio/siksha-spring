create table if not exists restaurant_v2
(
    id              bigint not null auto_increment,
    name            varchar(100) collate utf8mb4_unicode_ci not null comment '식당/코너명, 예: 301동 식사',
    building        varchar(50) collate utf8mb4_unicode_ci default null comment 'UI 그룹핑용 건물명, 예: 301동',
    address         varchar(200) collate utf8mb4_unicode_ci default null comment '주소',
    latitude        decimal(10, 7) default null comment '위도',
    longitude       decimal(10, 7) default null comment '경도',
    operating_hours json default null comment '요일별/끼니별 영업시간',
    owner_id        int default null comment '식당 관리자 ID',
    created_at      timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    updated_at      timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (id),
    constraint uk_restaurant_v2_name
        unique (name),
    constraint fk_restaurant_v2_owner
        foreign key (owner_id) references admin_user (id)
            on delete set null
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_restaurant_v2_building
    on restaurant_v2 (building);

create index idx_restaurant_v2_owner_id
    on restaurant_v2 (owner_id);

create table if not exists menu_v2
(
    id            bigint not null auto_increment,
    restaurant_id bigint not null comment '메뉴가 속한 식당/코너 ID',
    name          varchar(200) collate utf8mb4_unicode_ci not null comment '정규화된 대표 메뉴명',
    created_at    timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    primary key (id),
    constraint uk_menu_v2_restaurant_name
        unique (restaurant_id, name),
    constraint fk_menu_v2_restaurant
        foreign key (restaurant_id) references restaurant_v2 (id)
            on delete restrict
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_alias_v2
(
    id         bigint not null auto_increment,
    alias      varchar(300) collate utf8mb4_unicode_ci not null comment '크롤링 원본 메뉴명',
    menu_name  varchar(200) collate utf8mb4_unicode_ci not null comment '정규화된 대표 메뉴명',
    created_at timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    primary key (id),
    constraint uk_menu_alias_v2_alias
        unique (alias)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_menu_alias_v2_menu_name
    on menu_alias_v2 (menu_name);

create table if not exists meal_v2
(
    id            bigint not null auto_increment,
    restaurant_id bigint not null comment '식단이 제공되는 식당/코너 ID',
    date          date not null comment '식단 제공 날짜',
    type          enum ('BREAKFAST', 'LUNCH', 'DINNER') not null comment '식단 제공 타입',
    price         int default null comment '해당 메뉴의 가격',
    no_meat       tinyint(1) not null default 0 comment '고기 없는 메뉴 여부',
    created_at    timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    primary key (id),
    constraint fk_meal_v2_restaurant
        foreign key (restaurant_id) references restaurant_v2 (id)
            on delete restrict
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_meal_v2_restaurant_date
    on meal_v2 (restaurant_id, date);

create index idx_meal_v2_date
    on meal_v2 (date);

create table if not exists meal_menu_v2
(
    id            bigint not null auto_increment,
    meal_id       bigint not null comment '식단 row ID',
    menu_id       bigint not null comment '정규화된 대표 메뉴 ID',
    original_name varchar(300) collate utf8mb4_unicode_ci not null comment '정규화 전 원본 메뉴명',
    primary key (id),
    constraint uk_meal_menu_v2_meal_menu
        unique (meal_id, menu_id),
    constraint fk_meal_menu_v2_meal
        foreign key (meal_id) references meal_v2 (id)
            on delete cascade,
    constraint fk_meal_menu_v2_menu
        foreign key (menu_id) references menu_v2 (id)
            on delete restrict
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_meal_menu_v2_menu_id
    on meal_menu_v2 (menu_id);

create table if not exists review_v2
(
    id         bigint not null auto_increment,
    menu_id    bigint not null comment '리뷰 대상 정규화 메뉴 ID',
    user_id    int not null comment '리뷰를 작성한 사용자 ID',
    score      int not null comment '메뉴에 대한 평점',
    comment    mediumtext collate utf8mb4_unicode_ci comment '메뉴에 대한 리뷰',
    etc        mediumtext collate utf8mb4_unicode_ci comment '기타 정보(json 형태로 저장)',
    created_at timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    updated_at timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (id),
    constraint fk_review_v2_menu
        foreign key (menu_id) references menu_v2 (id)
            on delete restrict,
    constraint fk_review_v2_user
        foreign key (user_id) references user (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_review_v2_menu_id
    on review_v2 (menu_id);

create index idx_review_v2_user_id
    on review_v2 (user_id);

create table if not exists keyword_review_v2
(
    review_id         bigint not null comment '키워드 리뷰가 연결된 리뷰 ID',
    taste             int not null comment '맛 평가',
    price             int not null comment '가격 평가',
    food_composition  int not null comment '구성 평가',
    primary key (review_id),
    constraint fk_keyword_review_v2_review
        foreign key (review_id) references review_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_like_v2
(
    id         bigint not null auto_increment,
    user_id    int not null comment '좋아요를 남긴 유저',
    menu_id    bigint not null comment '좋아요 대상 정규화 메뉴 ID',
    is_liked   tinyint(1) default null comment '좋아요 여부',
    created_at timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    updated_at timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (id),
    constraint uk_menu_like_v2_menu_user
        unique (menu_id, user_id),
    constraint fk_menu_like_v2_user
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint fk_menu_like_v2_menu
        foreign key (menu_id) references menu_v2 (id)
            on delete restrict
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_menu_like_v2_user_id
    on menu_like_v2 (user_id);

create table if not exists review_like_v2
(
    id         bigint not null auto_increment,
    user_id    int not null comment '리뷰를 좋아한 사용자의 id',
    review_id  bigint not null comment '사용자가 좋아한 리뷰 id',
    created_at timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    updated_at timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (id),
    constraint uk_review_like_v2_review_user
        unique (review_id, user_id),
    constraint fk_review_like_v2_user
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint fk_review_like_v2_review
        foreign key (review_id) references review_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_review_like_v2_review_id
    on review_like_v2 (review_id);
