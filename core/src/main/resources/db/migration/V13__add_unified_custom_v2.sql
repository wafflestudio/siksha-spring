create table if not exists custom_v2
(
    user_id    int                                 not null comment 'User ID',
    customs    json                                not null comment 'Custom data',
    created_at timestamp default CURRENT_TIMESTAMP not null comment 'Created time',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Updated time',
    primary key (user_id),
    constraint custom_v2_user_fk
        foreign key (user_id) references user (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists building_custom_v2;

drop table if exists restaurant_custom_v2;
