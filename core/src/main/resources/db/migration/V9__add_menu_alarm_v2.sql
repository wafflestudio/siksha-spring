create table if not exists menu_alarm_v2
(
    user_id    int                                 not null comment 'User ID',
    menu_id    bigint                              not null comment 'Normalized V2 menu ID',
    created_at timestamp default CURRENT_TIMESTAMP not null comment 'Created time',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Updated time',
    primary key (user_id, menu_id),
    constraint menu_alarm_v2_user_fk
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint menu_alarm_v2_menu_fk
        foreign key (menu_id) references menu_v2 (id)
            on delete cascade
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create index menu_alarm_v2_menu_id_index
    on menu_alarm_v2 (menu_id);
