delete v1
from `version` v1
join `version` v2
    on v1.client_type = v2.client_type
    and v1.id < v2.id;

alter table `version`
    drop column `version`;

alter table `version`
    add constraint uk_version_client_type
        unique (client_type);
