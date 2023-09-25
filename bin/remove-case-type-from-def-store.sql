DROP TABLE IF EXISTS tmp_case_type_ids;

--Store just the filtered case_type ids to be used for deletion purposes
CREATE TABLE tmp_case_type_ids_for_:caseTypeReference AS
  SELECT id FROM case_type WHERE reference = :'caseTypeReference';

--All DELETION queries below use ids stored in TEMP table 'tmp_case_type_ids_for_:caseTypeReference'
--To remove all historical versions (therefore not use the filtered list of ids within TEMP table)
--Replace tmp_case_type_ids_for_:caseTypeReference with view__case_type_to_remove in all of the statements below. This will
--delete all historical versions older than X months (highest and most current version is not processed
--in this script.

DELETE FROM event_case_field_complex_type WHERE event_case_field_id IN
  (SELECT id FROM event_case_field WHERE event_id IN
      (SELECT id FROM event WHERE case_type_id IN
          (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
      )
  );

DELETE FROM event_case_field WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM challenge_question WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM display_group_case_field WHERE display_group_id IN
  (SELECT id FROM display_group WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM case_field_acl WHERE case_field_id IN
  (SELECT id FROM case_field WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM workbasket_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM workbasket_input_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_alias_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_result_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_input_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_cases_result_fields WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM complex_field_acl WHERE case_field_id IN
  (SELECT id FROM case_field WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

----takes very long to complete
----fk_case_field_case_type_id is not indexed by default
--DELETE FROM case_field WHERE case_type_id IN
--  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM display_group WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM event_webhook WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM event_pre_state WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM event_acl WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM event_post_state WHERE case_event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM state_acl WHERE state_id IN
  (SELECT id FROM state WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference)
  );

DELETE FROM state WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM case_type_acl WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

--Takes very long to complete (> 8min in AAT)
--fk_role_case_type_id_case_type_id is indexed by default
DELETE FROM role WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM role_to_access_profiles WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_criteria WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM search_party WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM category WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

----Takes very long to complete
DELETE FROM event WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DELETE FROM case_type WHERE id IN
  (SELECT id FROM tmp_case_type_ids_for_:caseTypeReference);

DROP TABLE tmp_case_type_ids_for_:caseTypeReference;
