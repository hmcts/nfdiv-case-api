DROP TABLE IF EXISTS tmp_case_type_ids_for_:changeId;

--Store just the filtered case_type ids to be used for deletion purposes
CREATE TABLE tmp_case_type_ids_for_:changeId AS
  SELECT id FROM case_type WHERE reference IN (:caseTypeReferences);

DELETE FROM event_case_field_complex_type WHERE event_case_field_id IN
  (SELECT id FROM event_case_field WHERE event_id IN
      (SELECT id FROM event WHERE case_type_id IN
          (SELECT id FROM tmp_case_type_ids_for_:changeId)
      )
  );

DELETE FROM event_case_field WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM challenge_question WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM display_group_case_field WHERE display_group_id IN
  (SELECT id FROM display_group WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM case_field_acl WHERE case_field_id IN
  (SELECT id FROM case_field WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM workbasket_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM workbasket_input_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_alias_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_result_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_input_case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_cases_result_fields WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM complex_field_acl WHERE case_field_id IN
  (SELECT id FROM case_field WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM case_field WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM display_group WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM event_webhook WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM event_pre_state WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM event_acl WHERE event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM event_post_state WHERE case_event_id IN
  (SELECT id FROM event WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM state_acl WHERE state_id IN
  (SELECT id FROM state WHERE case_type_id IN
      (SELECT id FROM tmp_case_type_ids_for_:changeId)
  );

DELETE FROM state WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM case_type_acl WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM role WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM role_to_access_profiles WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_criteria WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM search_party WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM category WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM event WHERE case_type_id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DELETE FROM case_type WHERE id IN
  (SELECT id FROM tmp_case_type_ids_for_:changeId);

DROP TABLE tmp_case_type_ids_for_:changeId;
