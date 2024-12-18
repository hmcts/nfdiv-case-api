create schema ccd;

create table ccd.submitted_callback_queue (
    id bigserial primary key,
    case_event_id bigint not null references case_event(id),
    event_id text not null,
    operation_id uuid,
    payload jsonb not null,
    headers jsonb not null,
    attempted_at timestamp,
    exception bytea,
    exception_message text
);

create view ccd.failed_jobs as
  select
    case_reference as reference,
    q.id as job_id,
    operation_id,
    q.attempted_at,
    ce.id,
    ce.event_id,
    ce.state_id,
    exception,
    exception_message
  from
    ccd.submitted_callback_queue q
    join case_event ce on ce.id = q.case_event_id
  where q.attempted_at is not null;
