create table if not exists `version`
(
    `id`              int not null auto_increment,
    `minimum_version` varchar(20) not null comment '최소 버전',
    `client_type`     varchar(10) not null comment '클라이언트 타입(AND, IOS, WEB)',
    `created_at`      timestamp not null default CURRENT_TIMESTAMP comment '생성 시간',
    `updated_at`      timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '변경 시간',
    primary key (`id`),
    constraint uk_version_client_type
        unique (`client_type`)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci;

delete v1
from `version` v1
join `version` v2
    on v1.client_type = v2.client_type
    and v1.id < v2.id;

set @version_column_exists = (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'version'
      and column_name = 'version'
);

set @drop_version_column_sql = if(
    @version_column_exists > 0,
    'alter table `version` drop column `version`',
    'select 1'
);

prepare stmt from @drop_version_column_sql;
execute stmt;
deallocate prepare stmt;

set @version_client_type_unique_exists = (
    select count(*)
    from information_schema.table_constraints
    where table_schema = database()
      and table_name = 'version'
      and constraint_name = 'uk_version_client_type'
      and constraint_type = 'UNIQUE'
);

set @add_version_client_type_unique_sql = if(
    @version_client_type_unique_exists = 0,
    'alter table `version` add constraint uk_version_client_type unique (`client_type`)',
    'select 1'
);

prepare stmt from @add_version_client_type_unique_sql;
execute stmt;
deallocate prepare stmt;
