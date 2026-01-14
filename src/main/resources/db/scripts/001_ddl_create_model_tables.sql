create table if not exists tasks
(
    id          serial primary key,
    name        varchar(255) not null,
    description text,
    viewed      boolean,
    status      varchar(25)  not null CHECK ( status in ('NEW', 'IN_PROGRESS', 'DONE')),
    type        varchar(25)  not null check ( type in ('TASK', 'EPIC', 'SUBTASK'))
);

create table if not exists epics
(
    id          serial primary key,
    name        varchar(25),
    description text,
    status      varchar(25) not null check ( status in ('NEW', 'IN_PROGRESS', 'DONE')),
    type        varchar(25) not null check ( type in ('TASK', 'EPIC', 'SUBTASK'))
);

create table if not exists subtasks
(
    id          serial primary key,
    name        varchar(25),
    description text,
    status      varchar(25) not null check ( status in ('NEW', 'IN_PROGRESS', 'DONE')),
    type        varchar(25) not null check ( type in ('TASK', 'EPIC', 'SUBTASK')),
    epic_id int references epics(id)
);
