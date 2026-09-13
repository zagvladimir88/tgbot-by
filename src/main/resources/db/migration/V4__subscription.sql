create table subscription (
    id          bigserial primary key,
    chat_id     bigint      not null,
    type        text        not null,
    payload     text        not null default '',
    cron        text        not null,
    zone_id     text        not null,
    next_run_at timestamptz not null,
    last_run_at timestamptz,
    enabled     boolean     not null default true,
    created_by  bigint      not null
);

create index subscription_due_idx on subscription (next_run_at) where enabled;
