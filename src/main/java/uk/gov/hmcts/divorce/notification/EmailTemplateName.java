package uk.gov.hmcts.divorce.notification;

public enum EmailTemplateName {
    SAVE_SIGN_OUT,
    OUTSTANDING_ACTIONS,
    APPLICATION_SUBMITTED,
    SOLICITOR_JOINT_APPLICATION_SUBMITTED,
    JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW,
    JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
    JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_APPLICANT1_REPRESENTED,
    JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_SOLICITOR,
    JOINT_APPLICANT2_REMINDER_WHEN_APPLICANT1_REPRESENTED,
    JOINT_APPLICANT2_REQUEST_CHANGES,
    APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES,
    SOLICITOR_APPLICANT2_REQUESTED_CHANGES,
    JOINT_APPLICANT1_APPLICANT2_APPROVED,
    JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF,
    JOINT_APPLICANT2_APPLICANT2_APPROVED,
    JOINT_APPLICANT2_APPLICANT2_APPROVED_SOLICITOR,
    JOINT_APPLICANT1_APPLICANT2_REJECTED,
    JOINT_APPLICANT2_APPLICANT2_REJECTED,
    JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR,
    JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES,
    JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE,
    JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE,
    JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR,
    JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE,
    JOINT_APPLICATION_ACCEPTED,
    JOINT_APPLICATION_OVERDUE,
    JOINT_APPLICATION_SUBMITTED,
    JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER,
    SOLE_APPLICANT_APPLICATION_ACCEPTED,
    SOLE_RESPONDENT_APPLICATION_ACCEPTED,
    OVERSEAS_RESPONDENT_APPLICATION_ISSUED,
    SOLE_APPLICANT_APPLICATION_SUBMITTED,
    SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED,
    SOLE_APPLICANT_AMENDED_APPLICATION_SUBMITTED,
    SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED,
    SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
    SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE,
    JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
    APPLICANT_NOTICE_OF_PROCEEDINGS,
    RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
    APPLICANT_SOLICITOR_SERVICE,
    GENERAL_EMAIL_PETITIONER,
    GENERAL_EMAIL_PETITIONER_SOLICITOR,
    GENERAL_EMAIL_RESPONDENT,
    GENERAL_EMAIL_RESPONDENT_SOLICITOR,
    GENERAL_EMAIL_OTHER_PARTY,
    SOLICITOR_AWAITING_CONDITIONAL_ORDER,
    JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
    APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER,
    SOLICITOR_CLARIFICATION_SUBMITTED,
    CITIZEN_CLARIFICATION_SUBMITTED,
    CITIZEN_PARTNER_CLARIFICATION_SUBMITTED,
    SOLICITOR_CO_REFUSED_SOLE_JOINT,
    CITIZEN_APPLICATION_WITHDRAWN,
    CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
    CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER,
    APPLICANT1_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER,
    CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
    JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
    JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER,
    JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER,
    SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
    SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED,
    JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
    CITIZEN_CONDITIONAL_ORDER_PRONOUNCED,
    CITIZEN_CONDITIONAL_ORDER_REFUSED,
    CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT,
    SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
    SOLE_APPLICANT_AOS_SUBMITTED,
    SOLE_RESPONDENT_AOS_SUBMITTED,
    SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED,
    SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED,
    SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR,
    SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR,
    SOLICITOR_APPLICANT1_DISPUTE_ANSWER_RECEIVED,
    BAILIFF_SERVICE_UNSUCCESSFUL,
    BAILIFF_SERVICE_SUCCESSFUL,
    DISPUTE_FORM_OVERDUE,
    APPLICANT_SWITCH_TO_SOLE,
    JOINT_APPLICATION_ENDED,
    PARTNER_SWITCHED_TO_SOLE_CO,
    APPLICANT_APPLY_FOR_FINAL_ORDER,
    RESPONDENT_APPLY_FOR_FINAL_ORDER,
    RESPONDENT_SOLICITOR_APPLY_FOR_FINAL_ORDER,
    SOLE_APPLIED_FOR_FINAL_ORDER,
    JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER,
    JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
    POST_INFORMATION_TO_COURT,
    GENERAL_APPLICATION_RECEIVED,
    GENERAL_APPLICATION_SUCCESSFUL,
    SERVICE_APPLICATION_REJECTED,
    SERVICE_APPLICATION_GRANTED,
    RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED,
    APPLY_FOR_FINAL_ORDER_SOLICITOR,
    JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR,
    JOINT_APPLICANT_CAN_SWITCH_TO_SOLE,
    JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE,
    JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER,
    JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
    SOLICITOR_FINAL_ORDER_GRANTED,
    APPLICANTS_FINAL_ORDER_GRANTED,
    SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER,
    SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER,
    JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
    JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER,
    INTEND_TO_SWITCH_TO_SOLE_FO,
    PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
    OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER,
    PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER,
    FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_APPLICANT,
    FINAL_ORDER_GRANTED_SWITCH_TO_SOLE_RESPONDENT,
    OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
    OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN,
    APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
    SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER,
    APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
    APPLICANT_2_SOLICITOR_APPLIED_FOR_FINAL_ORDER,
    NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN,
    NOC_TO_SOLS_EMAIL_NEW_SOL,
    NOC_TO_SOLS_EMAIL_OLD_SOL
}
