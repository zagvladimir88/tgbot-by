create table cse_quota (
    day  date primary key,
    used integer not null default 0
);
