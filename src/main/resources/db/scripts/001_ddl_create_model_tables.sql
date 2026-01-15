create table if not exists tasks
(
    id          serial primary key,
    name        varchar(255) not null,
    description text,
    viewed      boolean      not null,
    status      varchar(25),
    type        varchar(25),
    epic_id     int references tasks (id)
);

alter table tasks add constraint allowed_statuses
    check (status is not null and status in ('NEW', 'IN_PROGRESS', 'DONE'));

alter table tasks add constraint allowed_type
    check (type is not null and type in ('TASK', 'EPIC', 'SUBTASK'));

alter table tasks add constraint type_id_compatibility
    check (
        (type = 'SUBTASK' and epic_id is not null and epic_id != id)
            or
        (type != 'SUBTASK' and epic_id is null)
        );
