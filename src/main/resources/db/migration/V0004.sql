
create table multiples (
  multiple_id serial primary key,
  lead_case_id bigint not null unique,
  name text not null
);

create table multiple_members(
    multiple_id bigint references multiples(multiple_id) on delete cascade,
    -- A case can only be in a single multiple
    sub_case_id bigint unique references case_data(reference) on delete cascade,
    primary key (multiple_id, sub_case_id)
);

-- A multiple points to its lead case in a circular reference
alter table multiples
    add foreign key (multiple_id, lead_case_id)
    references multiple_members(multiple_id, sub_case_id)
    deferrable initially deferred;

create view sub_cases as (
  select
  m.name,
  lead_case_id,
  sub_case_id,
  cd.last_modified,
  cd.data->>'applicant1FirstName' applicant1FirstName,
  cd.data->>'applicant1LastName' applicant1LastName,
  cd.data->>'applicant2FirstName' applicant2FirstName,
  cd.data->>'applicant2LastName' applicant2LastName
  from
    multiples m
    join multiple_members s using (multiple_id)
    join case_data cd on cd.reference = s.sub_case_id
  order by cd.last_modified desc
);





