create table es_queue (
  reference bigint references case_data(reference) primary key,
  id bigint references case_event(id)
);

create function public.add_to_es_queue() returns trigger
  language plpgsql
    as $$
begin
  insert into es_queue (reference, id)
  values (new.case_reference, new.id)
  on conflict (reference)
                do update set id = excluded.id
  where es_queue.id < excluded.id;
return new;
end $$;

create trigger after_case_event_insert
  after insert on case_event
  for each row
  execute function add_to_es_queue();

create or replace function update_last_state_modified_date()
returns trigger as $$
begin
    -- check if the state field has changed
    if new.state is distinct from old.state then
        -- update the last_state_modified_date to the current timestamp
        new.last_state_modified_date := now();
end if;
return new;
end;
$$ language plpgsql;

create trigger trigger_update_last_state_modified_date
  before insert or update on case_data
  for each row
  execute function update_last_state_modified_date();
