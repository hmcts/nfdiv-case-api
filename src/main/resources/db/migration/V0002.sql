create table case_notes(
   reference bigint references case_data(reference) ,
   id bigserial,
   date date not null,
   note varchar(10000),
   author varchar(200) not null,
   primary key(reference, id)
);

insert into case_notes(reference, id, date, note, author)
select
  reference,
  (note->>'id')::bigint,
  (note->'value'->>'date')::date,
  note->'value'->>'note',
  note->'value'->>'author'
from
  case_data,
  jsonb_array_elements(data->'notes') note;


create view notes_by_case as
select
reference,
jsonb_agg(
  json_build_object(
    'value', jsonb_build_object(
      'date', date,
      'note', note,
      'author', author
    )
  -- Ensure most recent case notes are first
  ) order by id desc
) notes from case_notes
group by reference;
