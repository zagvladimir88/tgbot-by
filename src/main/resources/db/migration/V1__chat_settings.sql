create table chat_settings (
    chat_id            bigint primary key,
    default_city       text,
    default_currencies text[] not null default '{USD,EUR,RUB}',
    zone_id            text   not null default 'Europe/Minsk',
    updated_at         timestamptz not null default now()
);
