create table if not exists history
(
    task_id   int references tasks (id) on delete cascade  primary key,
    viewed_at timestamp default current_timestamp,
    type      varchar(25)
);
