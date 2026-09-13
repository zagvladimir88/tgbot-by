create table rate_alert (
    id                bigserial primary key,
    chat_id           bigint  not null,
    currency          text    not null,
    condition         text    not null,
    threshold         numeric not null,
    one_shot          boolean not null default false,
    enabled           boolean not null default true,
    last_fired_on_date date,
    created_by        bigint  not null
);

create index rate_alert_enabled_idx on rate_alert (enabled) where enabled;
