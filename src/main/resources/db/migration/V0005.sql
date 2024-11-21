-- Subcases can derive certain fields from the lead case
create view derived_cases as
select
  sub_case_id,
  subcase.data
    || jsonb_build_object('applicant1Address', parent.data->'applicant1Address') as data
from multiple_members
       join multiples m using (multiple_id)
       join case_data parent on parent.reference = m.lead_case_id
       join case_data subcase on subcase.reference = sub_case_id
