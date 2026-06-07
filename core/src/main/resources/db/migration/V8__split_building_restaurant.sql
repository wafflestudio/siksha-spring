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
    add column display_order int not null default 0 after owner_id,
    add column active tinyint(1) not null default 1 after display_order;

alter table restaurant_v2
    drop index idx_restaurant_v2_building;

alter table restaurant_v2
    drop index uk_restaurant_v2_name;

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

update restaurant_v2 r join building_v2 b on b.number = '63동'
set r.building_id = b.id, r.display_order = 1, r.active = 1
where r.name = '학생회관식당';

update restaurant_v2 r join building_v2 b on b.number = '75-1동'
set r.building_id = b.id,
    r.display_order = case r.name
        when '3식당' then 1
        when '4층 푸드코드' then 90
        when '두레미담' then 91
        when '버거운버거' then 99
        else r.display_order
    end,
    r.active = case r.name
        when '3식당' then 1
        else 0
    end
where r.name in ('3식당', '4층 푸드코드', '두레미담', '버거운버거');

update restaurant_v2 r join building_v2 b on b.number = '919동'
set r.building_id = b.id, r.display_order = 90, r.active = 0
where r.name = '생협기숙사(919동)';

update restaurant_v2 r join building_v2 b on b.number = '901동'
set r.building_id = b.id, r.display_order = 90, r.active = 0
where r.name = '아워홈(901동)';

update restaurant_v2 r join building_v2 b on b.number = '302동'
set r.building_id = b.id, r.display_order = 1, r.active = 1
where r.name = '302동식당';

update restaurant_v2 r join building_v2 b on b.number = '301동'
set r.building_id = b.id, r.display_order = 90, r.active = 0
where r.name = '301동식당';

update restaurant_v2 r join building_v2 b on b.number = '113동'
set r.building_id = b.id, r.display_order = 1, r.active = 1
where r.name = '동원관식당';

update restaurant_v2 r join building_v2 b on b.number = '109동'
set r.building_id = b.id,
    r.display_order = case r.name
        when '자하연식당 2층' then 1
        when '자하연식당 3층' then 2
        else r.display_order
    end,
    r.active = 1
where r.name in ('자하연식당 2층', '자하연식당 3층');

update restaurant_v2 r join building_v2 b on b.number = '74동'
set r.building_id = b.id, r.display_order = 90, r.active = 0
where r.name = '예술계식당';

update restaurant_v2 r join building_v2 b on b.number = '30-2동'
set r.building_id = b.id, r.display_order = 1, r.active = 1
where r.name = '공대간이식당';

update restaurant_v2 r join building_v2 b on b.number = '220동'
set r.building_id = b.id, r.display_order = 90, r.active = 0
where r.name = '220동식당';

update restaurant_v2 r join building_v2 b on b.number = '85동'
set r.building_id = b.id, r.display_order = 1, r.active = 1
where r.name = '수의대식당';

insert into restaurant_v2 (building_id, name, display_order, active)
select b.id, x.name, x.display_order, 1
from building_v2 b
join (
    select '63동' building_number, '학생회관식당' name, 1 display_order union all
    select '75-1동', '3식당', 1 union all
    select '75-1동', '서가앤쿡', 2 union all
    select '75-1동', '토끼정', 3 union all
    select '75-1동', '숨쉬는순두부', 4 union all
    select '75-1동', '이공오 돈까스와 우동', 5 union all
    select '75-1동', '셀프코너', 6 union all
    select '75-1동', '식당', 7 union all
    select '919동', '생협기숙사', 1 union all
    select '901동', '아워홈', 1 union all
    select '302동', '302동식당', 1 union all
    select '301동', '일반', 1 union all
    select '301동', 'TAKE-OUT', 2 union all
    select '301동', '1층 교직원전용식당', 3 union all
    select '301동', '카페 301동', 4 union all
    select '301동', '키친101', 5 union all
    select '113동', '동원관식당', 1 union all
    select '109동', '자하연식당 2층', 1 union all
    select '109동', '자하연식당 3층', 2 union all
    select '74동', 'A코너', 1 union all
    select '74동', 'B코너', 2 union all
    select '74동', 'C코너', 3 union all
    select '74동', '직화코너', 4 union all
    select '30-2동', '공대간이식당', 1 union all
    select '220동', '경성 돈카츠', 1 union all
    select '220동', '바비든든', 2 union all
    select '220동', '포포420', 3 union all
    select '220동', '값찌개', 4 union all
    select '85동', '수의대식당', 1
) x on x.building_number = b.number
where not exists (
    select 1
    from restaurant_v2 r
    where r.building_id = b.id
      and r.name = x.name
);

update restaurant_v2 r
join building_v2 b on b.number = '기타'
set r.building_id = b.id, r.display_order = 999, r.active = 0
where r.building_id is null;

alter table restaurant_v2
    modify column building_id int not null,
    add constraint uk_restaurant_v2_building_name unique (building_id, name),
    add constraint fk_restaurant_v2_building
        foreign key (building_id) references building_v2 (id)
            on delete restrict;

create index idx_restaurant_v2_building_active
    on restaurant_v2 (building_id, active, display_order);
