create table rate_snapshot (
    on_date    date primary key,
    fetched_at timestamptz not null default now(),
    payload    jsonb       not null
);

create index rate_snapshot_fetched_at_idx on rate_snapshot (fetched_at desc);
