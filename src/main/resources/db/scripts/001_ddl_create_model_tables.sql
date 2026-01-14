create table if not exists tasks
(
    id          serial primary key,
    name        varchar(255) not null,
    description text,
    viewed      boolean not null ,
    status      varchar(25)  not null CHECK ( status in ('NEW', 'IN_PROGRESS', 'DONE')),
    type        varchar(25)  not null check ( type in ('TASK', 'EPIC', 'SUBTASK')),
    epic_id     int references tasks (id)
);
