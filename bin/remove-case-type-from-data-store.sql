
-- DELETE FROM submitted WHERE case_type_id = :'caseTypeReference';
-- DELETE FROM event_data_report WHERE case_type_id = :'caseTypeReference';
DELETE FROM case_event WHERE case_type_id = :'caseTypeReference';
DELETE FROM case_data WHERE case_type_id = :'caseTypeReference';
