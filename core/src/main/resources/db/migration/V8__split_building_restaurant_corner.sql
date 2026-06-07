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

alter table restaurant_v2
    add column building_id int null after id,
    add column display_order int not null default 0 after owner_id;

alter table restaurant_v2
    drop index uk_restaurant_v2_name;

alter table restaurant_v2
    add constraint uk_restaurant_v2_building_name unique (building_id, name);

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

update restaurant_v2
set name = '4층 푸드코드'
where name = '75-1동 4층 푸드코트';

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '학생회관식당', 1
from building_v2 b
where b.number = '63동'
  and not exists (select 1 from restaurant_v2 r where r.name = '학생회관식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '3식당', 1
from building_v2 b
where b.number = '75-1동'
  and not exists (select 1 from restaurant_v2 r where r.name = '3식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '4층 푸드코드', 2
from building_v2 b
where b.number = '75-1동'
  and not exists (select 1 from restaurant_v2 r where r.name = '4층 푸드코드');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '두레미담', 3
from building_v2 b
where b.number = '75-1동'
  and not exists (select 1 from restaurant_v2 r where r.name = '두레미담');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '생협기숙사(919동)', 1
from building_v2 b
where b.number = '919동'
  and not exists (select 1 from restaurant_v2 r where r.name = '생협기숙사(919동)');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '아워홈(901동)', 1
from building_v2 b
where b.number = '901동'
  and not exists (select 1 from restaurant_v2 r where r.name = '아워홈(901동)');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '302동식당', 1
from building_v2 b
where b.number = '302동'
  and not exists (select 1 from restaurant_v2 r where r.name = '302동식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '301동식당', 1
from building_v2 b
where b.number = '301동'
  and not exists (select 1 from restaurant_v2 r where r.name = '301동식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '동원관식당', 1
from building_v2 b
where b.number = '113동'
  and not exists (select 1 from restaurant_v2 r where r.name = '동원관식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '자하연식당 2층', 1
from building_v2 b
where b.number = '109동'
  and not exists (select 1 from restaurant_v2 r where r.name = '자하연식당 2층');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '자하연식당 3층', 2
from building_v2 b
where b.number = '109동'
  and not exists (select 1 from restaurant_v2 r where r.name = '자하연식당 3층');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '예술계식당', 1
from building_v2 b
where b.number = '74동'
  and not exists (select 1 from restaurant_v2 r where r.name = '예술계식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '공대간이식당', 1
from building_v2 b
where b.number = '30-2동'
  and not exists (select 1 from restaurant_v2 r where r.name = '공대간이식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '220동식당', 1
from building_v2 b
where b.number = '220동'
  and not exists (select 1 from restaurant_v2 r where r.name = '220동식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '수의대식당', 1
from building_v2 b
where b.number = '85동'
  and not exists (select 1 from restaurant_v2 r where r.name = '수의대식당');

insert into restaurant_v2 (building_id, name, display_order)
select b.id, '버거운버거', 4
from building_v2 b
where b.number = '75-1동'
  and not exists (select 1 from restaurant_v2 r where r.name = '버거운버거');

update restaurant_v2 r join building_v2 b on b.number = '63동'
set r.building_id = b.id, r.display_order = 1
where r.name = '학생회관식당';

update restaurant_v2 r join building_v2 b on b.number = '75-1동'
set r.building_id = b.id,
    r.display_order = case r.name
        when '3식당' then 1
        when '4층 푸드코드' then 2
        when '두레미담' then 3
        when '버거운버거' then 4
        else r.display_order
    end
where r.name in ('3식당', '4층 푸드코드', '두레미담', '버거운버거');

update restaurant_v2 r join building_v2 b on b.number = '919동'
set r.building_id = b.id, r.display_order = 1
where r.name = '생협기숙사(919동)';

update restaurant_v2 r join building_v2 b on b.number = '901동'
set r.building_id = b.id, r.display_order = 1
where r.name = '아워홈(901동)';

update restaurant_v2 r join building_v2 b on b.number = '302동'
set r.building_id = b.id, r.display_order = 1
where r.name = '302동식당';

update restaurant_v2 r join building_v2 b on b.number = '301동'
set r.building_id = b.id, r.display_order = 1
where r.name = '301동식당';

update restaurant_v2 r join building_v2 b on b.number = '113동'
set r.building_id = b.id, r.display_order = 1
where r.name = '동원관식당';

update restaurant_v2 r join building_v2 b on b.number = '109동'
set r.building_id = b.id,
    r.display_order = case r.name
        when '자하연식당 2층' then 1
        when '자하연식당 3층' then 2
        else r.display_order
    end
where r.name in ('자하연식당 2층', '자하연식당 3층');

update restaurant_v2 r join building_v2 b on b.number = '74동'
set r.building_id = b.id, r.display_order = 1
where r.name = '예술계식당';

update restaurant_v2 r join building_v2 b on b.number = '30-2동'
set r.building_id = b.id, r.display_order = 1
where r.name = '공대간이식당';

update restaurant_v2 r join building_v2 b on b.number = '220동'
set r.building_id = b.id, r.display_order = 1
where r.name = '220동식당';

update restaurant_v2 r join building_v2 b on b.number = '85동'
set r.building_id = b.id, r.display_order = 1
where r.name = '수의대식당';

update restaurant_v2 r join building_v2 b on b.number = '기타'
set r.building_id = b.id, r.display_order = 999
where r.building_id is null;

alter table restaurant_v2
    modify column building_id int not null,
    add constraint fk_restaurant_v2_building
        foreign key (building_id) references building_v2 (id)
            on delete restrict;

create table if not exists corner_v2
(
    id            int not null auto_increment,
    restaurant_id int not null,
    name          varchar(100) collate utf8mb4_unicode_ci default null comment '코너명. 단일 코너 식당은 null',
    is_default    tinyint(1) not null default 0 comment '단일 코너 또는 legacy 집계 데이터용 기본 코너',
    active        tinyint(1) not null default 1 comment '클라이언트에 노출할 코너 여부',
    display_order int not null default 0,
    created_at    timestamp not null default CURRENT_TIMESTAMP,
    updated_at    timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_corner_v2_restaurant
        foreign key (restaurant_id) references restaurant_v2 (id)
            on delete cascade,
    constraint uk_corner_v2_restaurant_name
        unique (restaurant_id, name)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index idx_corner_v2_restaurant_active
    on corner_v2 (restaurant_id, active);

insert into corner_v2 (restaurant_id, name, is_default, active, display_order)
select r.id, null, 1, 1, 1
from restaurant_v2 r
where r.name in (
    '학생회관식당', '3식당', '생협기숙사(919동)', '아워홈(901동)', '302동식당',
    '동원관식당', '자하연식당 2층', '자하연식당 3층', '공대간이식당', '수의대식당'
)
  and not exists (select 1 from corner_v2 c where c.restaurant_id = r.id and c.is_default = 1);

insert into corner_v2 (restaurant_id, name, is_default, active, display_order)
select r.id, null, 1, 0, 0
from restaurant_v2 r
where r.name in ('4층 푸드코드', '두레미담', '301동식당', '예술계식당', '220동식당', '버거운버거')
  and not exists (select 1 from corner_v2 c where c.restaurant_id = r.id and c.is_default = 1);

insert into corner_v2 (restaurant_id, name, is_default, active, display_order)
select r.id, x.name, 0, 1, x.display_order
from restaurant_v2 r
join (
    select '4층 푸드코드' restaurant_name, '서가앤쿡' name, 1 display_order union all
    select '4층 푸드코드', '토끼정', 2 union all
    select '4층 푸드코드', '숨쉬는순두부', 3 union all
    select '4층 푸드코드', '이공오 돈까스와 우동', 4 union all
    select '두레미담', '셀프코너', 1 union all
    select '두레미담', '식당', 2 union all
    select '301동식당', '일반', 1 union all
    select '301동식당', 'TAKE-OUT', 2 union all
    select '301동식당', '1층 교직원전용식당', 3 union all
    select '301동식당', '카페 301동', 4 union all
    select '301동식당', '키친101', 5 union all
    select '예술계식당', 'A코너', 1 union all
    select '예술계식당', 'B코너', 2 union all
    select '예술계식당', 'C코너', 3 union all
    select '예술계식당', '직화코너', 4 union all
    select '220동식당', '경성 돈카츠', 1 union all
    select '220동식당', '바비든든', 2 union all
    select '220동식당', '포포420', 3 union all
    select '220동식당', '값찌개', 4
) x on x.restaurant_name = r.name
where not exists (
    select 1
    from corner_v2 c
    where c.restaurant_id = r.id
      and c.name = x.name
);

alter table menu_v2
    drop foreign key fk_menu_v2_restaurant;

alter table meal_v2
    drop foreign key fk_meal_v2_restaurant;

alter table menu_v2
    drop index uk_menu_v2_restaurant_name;

alter table menu_v2
    add column corner_id int null after id;

alter table meal_v2
    add column corner_id int null after id;

update menu_v2 m
join restaurant_v2 r on r.id = m.restaurant_id
join corner_v2 c on c.restaurant_id = r.id and c.is_default = 1
set m.corner_id = c.id
where m.corner_id is null;

update meal_v2 m
join restaurant_v2 r on r.id = m.restaurant_id
join corner_v2 c on c.restaurant_id = r.id and c.is_default = 1
set m.corner_id = c.id
where m.corner_id is null;

insert into corner_v2 (restaurant_id, name, is_default, active, display_order)
select r.id, null, 1, 0, 0
from restaurant_v2 r
where not exists (select 1 from corner_v2 c where c.restaurant_id = r.id);

update menu_v2 m
join restaurant_v2 r on r.id = m.restaurant_id
join corner_v2 c on c.restaurant_id = r.id and c.is_default = 1
set m.corner_id = c.id
where m.corner_id is null;

update meal_v2 m
join restaurant_v2 r on r.id = m.restaurant_id
join corner_v2 c on c.restaurant_id = r.id and c.is_default = 1
set m.corner_id = c.id
where m.corner_id is null;

alter table menu_v2
    modify column restaurant_id int null comment 'legacy restaurant id; use corner_id for v2',
    modify column corner_id int not null,
    add constraint uk_menu_v2_corner_name unique (corner_id, name),
    add constraint fk_menu_v2_corner
        foreign key (corner_id) references corner_v2 (id)
            on delete restrict;

alter table meal_v2
    modify column restaurant_id int null comment 'legacy restaurant id; use corner_id for v2',
    modify column corner_id int not null,
    add constraint fk_meal_v2_corner
        foreign key (corner_id) references corner_v2 (id)
            on delete restrict;

create index idx_meal_v2_corner_date
    on meal_v2 (corner_id, date);

create table if not exists corner_custom_v2
(
    user_id       int not null comment '사용자 ID',
    corner_id     int not null comment '코너 ID',
    `like`        tinyint(1) not null default 0 comment '좋아요 여부',
    visible       tinyint(1) not null default 1 comment '노출 여부',
    order_index   int null comment '사용자 지정 정렬 순서',
    created_at    timestamp default CURRENT_TIMESTAMP not null,
    updated_at    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    primary key (user_id, corner_id),
    constraint corner_custom_v2_user_fk
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint corner_custom_v2_corner_fk
        foreign key (corner_id) references corner_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

insert into corner_custom_v2 (user_id, corner_id, `like`, visible, order_index, created_at, updated_at)
select rc.user_id, c.id, rc.`like`, rc.visible, rc.order_index, rc.created_at, rc.updated_at
from restaurant_custom_v2 rc
join corner_v2 c on c.restaurant_id = rc.restaurant_id and c.is_default = 1
where not exists (
    select 1
    from corner_custom_v2 cc
    where cc.user_id = rc.user_id
      and cc.corner_id = c.id
);
