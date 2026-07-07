START TRANSACTION;

ALTER TABLE menu_v2
    DROP FOREIGN KEY fk_menu_v2_restaurant;
ALTER TABLE meal_v2
    DROP FOREIGN KEY fk_meal_v2_restaurant;

ALTER TABLE restaurant_v2
    MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT;

ALTER TABLE menu_v2
    MODIFY COLUMN restaurant_id INT NOT NULL COMMENT '메뉴가 속한 식당/코너 ID';
ALTER TABLE meal_v2
    MODIFY COLUMN restaurant_id INT NOT NULL COMMENT '식단이 제공되는 식당/코너 ID';

ALTER TABLE menu_v2
    ADD CONSTRAINT fk_menu_v2_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_v2 (id)
    ON DELETE RESTRICT;
ALTER TABLE meal_v2
    ADD CONSTRAINT fk_meal_v2_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_v2 (id)
    ON DELETE RESTRICT;

create table if not exists restaurant_custom_v2
(
    user_id       int                                 not null comment '사용자 ID',
    restaurant_id int                                 not null comment '식당 ID',
    `like`        tinyint(1)                          not null default 0 comment '좋아요 여부',
    visible       tinyint(1)                          not null default 1 comment '노출 여부',
    order_index   int                                 null comment '사용자 지정 정렬 순서',
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (user_id, restaurant_id),
    constraint restaurant_custom_v2_user_fk
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint restaurant_custom_v2_restaurant_fk
        foreign key (restaurant_id) references restaurant_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

COMMIT;
