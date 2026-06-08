set foreign_key_checks = 0;

delete from keyword_review_v2;
delete from review_like_v2;
delete from menu_like_v2;
delete from meal_menu_v2;
delete from review_v2;
delete from meal_v2;
delete from menu_v2;
delete from restaurant_custom_v2;
delete from restaurant_v2;

set foreign_key_checks = 1;

alter table restaurant_v2 auto_increment = 1;
alter table menu_v2 auto_increment = 1;
alter table meal_v2 auto_increment = 1;
alter table meal_menu_v2 auto_increment = 1;
alter table review_v2 auto_increment = 1;
alter table menu_like_v2 auto_increment = 1;
alter table review_like_v2 auto_increment = 1;

create table if not exists building_v2
(
    id         int not null auto_increment,
    number     varchar(20) collate utf8mb4_unicode_ci not null comment '건물 번호, 예: 63동',
    name       varchar(100) collate utf8mb4_unicode_ci default null comment '건물명, 예: 학생회관',
    sort_order int not null default 0 comment '화면 표시 순서',
    created_at timestamp not null default CURRENT_TIMESTAMP,
    updated_at timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint uk_building_v2_number unique (number)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists building_custom_v2
(
    user_id     int                                 not null comment '사용자 ID',
    building_id int                                 not null comment '건물 ID',
    order_index int                                 null comment '사용자 지정 건물 정렬 순서',
    created_at  timestamp default CURRENT_TIMESTAMP not null comment '생성 시간',
    updated_at  timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (user_id, building_id),
    constraint building_custom_v2_user_fk
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint building_custom_v2_building_fk
        foreign key (building_id) references building_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table restaurant_custom_v2
    modify column order_index int null comment '사용자 지정 건물 내부 식당 정렬 순서';

alter table restaurant_v2
    add column building_id int null after id,
    add column display_order int not null default 0 after owner_id;

alter table restaurant_v2
    drop index idx_restaurant_v2_building;

alter table restaurant_v2
    drop index uk_restaurant_v2_name;

alter table restaurant_v2
    drop column building;

insert into building_v2 (number, name, sort_order)
values ('63동', '학생회관', 1),
       ('75-1동', '전망대', 2),
       ('919동', '관악생활관', 3),
       ('901동', null, 4),
       ('302동', null, 5),
       ('301동', null, 6),
       ('113동', null, 7),
       ('109동', '농협', 8),
       ('74동', null, 9),
       ('30-2동', null, 10),
       ('220동', null, 11),
       ('85동', '수의과대학', 12),
       ('기타', null, 999)
on duplicate key update
    name = values(name),
    sort_order = values(sort_order);

insert into restaurant_v2 (building_id, name, display_order)
select b.id, x.name, x.display_order
from building_v2 b
join (
    select '63동' building_number, '학생회관식당' name, 1 display_order union all
    select '75-1동', '3식당 일반', 1 union all
    select '75-1동', '3식당 든든한끼샐러드코너', 2 union all
    select '75-1동', '4층 푸드코드 서가앤쿡', 3 union all
    select '75-1동', '4층 푸드코드 토끼정', 4 union all
    select '75-1동', '4층 푸드코드 숨쉬는순두부', 5 union all
    select '75-1동', '4층 푸드코드 이공오 돈까스와 우동', 6 union all
    select '75-1동', '두레미담 셀프코너', 7 union all
    select '75-1동', '두레미담 식당', 8 union all
    select '919동', '생협기숙사', 1 union all
    select '901동', '아워홈', 1 union all
    select '302동', '302동식당', 1 union all
    select '301동', '301동식당 천원의아침밥', 1 union all
    select '301동', '301동식당 일반', 2 union all
    select '301동', '301동식당 TAKE-OUT', 3 union all
    select '301동', '301동 1층 교직원전용식당', 4 union all
    select '301동', '카페 301', 5 union all
    select '113동', '동원관식당', 1 union all
    select '109동', '자하연식당 2층', 1 union all
    select '109동', '자하연식당 3층', 2 union all
    select '74동', '예술계식당 A코너', 1 union all
    select '74동', '예술계식당 B코너', 2 union all
    select '74동', '예술계식당 C코너', 3 union all
    select '74동', '예술계식당 직화코너', 4 union all
    select '30-2동', '공대간이식당', 1 union all
    select '220동', '220동식당 경성 돈카츠', 1 union all
    select '220동', '220동식당 바비든든', 2 union all
    select '220동', '220동식당 포포420', 3 union all
    select '220동', '220동식당 값찌개', 4 union all
    select '220동', '220동식당 키친101', 5 union all
    select '85동', '수의대식당', 1
) x on x.building_number = b.number;

alter table restaurant_v2
    modify column building_id int not null,
    add constraint uk_restaurant_v2_building_name unique (building_id, name),
    add constraint fk_restaurant_v2_building
        foreign key (building_id) references building_v2 (id)
            on delete restrict;

create index idx_restaurant_v2_building_order
    on restaurant_v2 (building_id, display_order);
