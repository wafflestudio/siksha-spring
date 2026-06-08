alter table building_v2
    add column address varchar(200) collate utf8mb4_unicode_ci default null comment '주소' after name,
    add column latitude decimal(10, 7) default null comment '위도' after address,
    add column longitude decimal(10, 7) default null comment '경도' after latitude;

update building_v2 b
join (
    select building_id,
           max(address) as address,
           max(latitude) as latitude,
           max(longitude) as longitude
    from restaurant_v2
    group by building_id
) r on r.building_id = b.id
set b.address = coalesce(b.address, r.address),
    b.latitude = coalesce(b.latitude, r.latitude),
    b.longitude = coalesce(b.longitude, r.longitude);

alter table restaurant_v2
    drop column address,
    drop column latitude,
    drop column longitude;

alter table restaurant_custom_v2
    drop column order_index;

alter table building_custom_v2
    add constraint uk_building_custom_v2_user_order unique (user_id, order_index);

alter table building_v2
    drop column sort_order;
