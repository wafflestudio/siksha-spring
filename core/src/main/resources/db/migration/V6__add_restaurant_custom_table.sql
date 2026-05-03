create table if not exists restaurant_custom
(
    user_id       int                                 not null comment '사용자 ID',
    restaurant_id int                                 not null comment '식당 ID',
    `like`        tinyint(1)                          not null default 0 comment '좋아요 여부',
    visible       tinyint(1)                          not null default 1 comment '노출 여부',
    order_index   int                                 null comment '사용자 지정 정렬 순서',
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (user_id, restaurant_id),
    constraint restaurant_custom_user_fk
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint restaurant_custom_restaurant_fk
        foreign key (restaurant_id) references restaurant (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index restaurant_custom_restaurant_id_index
    on restaurant_custom (restaurant_id);
