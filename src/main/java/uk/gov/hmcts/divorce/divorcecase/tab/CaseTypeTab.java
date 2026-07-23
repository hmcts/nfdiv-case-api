package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.State.LAReview;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_FEE_REQUIRED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_JUDGE_OR_LEGAL_ADVISOR_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_MEDIUM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_OUTCOMES;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.AMENDED_APPLICATIONS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.AOS_IS_DRAFTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APP2_SOL_FO_HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_FINAL_ORDER_LATE_EXPLANATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_FINAL_ORDER_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_FLAGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_SOLICITOR_FLAGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_APPLIED_FOR_FINAL_ORDER_FIRST;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_FINAL_ORDER_EXPLANATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_FINAL_ORDER_LATE_EXPLANATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_FINAL_ORDER_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_FLAGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOLICITOR_FLAGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOL_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOL_FINAL_ORDER_WHY_NEED_TO_APPLY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT2_SOL_RESPONSIBLE_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FINAL_ORDER_LATE_EXPLANATION_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_HWF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_IN_REFUGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LEGAL_PROCEEDINGS_DETAILS_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_PHONE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_REPRESENTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_USED_WELSH_TRANSLATION_ON_SUBMISSION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FINAL_ORDER_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FO_HWF_NEED_HELP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FO_HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_HWF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_HWF_NEED_HELP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_IN_REFUGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS_CONCLUDED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_PHONE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_REPRESENTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOL_FINAL_ORDER_FEE_ACCOUNT_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOL_FINAL_ORDER_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOL_PAYMENT_HOW_TO_PAY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_USED_WELSH_TRANSLATION_ON_SUBMISSION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_WELSH_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.BULK_LIST_CASE_REFERENCE_LINK;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CERTIFICATE_OF_SERVICE_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CHANGE_OF_REPRESENTATIVES;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CONFIDENTIAL_DOCUMENTS_GENERATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CONFIDENTIAL_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CONFIRM_READ_PETITION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.COUNTRY_LIFE_BASED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_CHANGE_OR_ADD_TO_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_CONFIRM_INFORMATION_STILL_CORRECT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_REASON_INFORMATION_NOT_CORRECT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_REASON_INFORMATION_NOT_CORRECT_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_SOLICITOR_ADDITIONAL_COMMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_1_SUBMITTED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_CHANGE_OR_ADD_TO_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_CONFIRM_INFORMATION_STILL_CORRECT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_REASON_INFORMATION_NOT_CORRECT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_REASON_INFORMATION_NOT_CORRECT_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_SOLICITOR_ADDITIONAL_COMMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_APPLICANT_2_SUBMITTED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CANNOT_UPLOAD_CLARIFICATION_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CERTIFICATE_OF_ENTITLEMENT_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CLAIMS_COSTS_ORDER_INFORMATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CLAIMS_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CLARIFICATION_RESPONSES_SUBMITTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_CONDITIONAL_ORDER_GRANTED_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_COURT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_DATE_AND_TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_DECISION_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_GRANTED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_JUDGE_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_JUDGE_COSTS_ORDER_ADDITIONAL_INFO;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_LEGAL_ADVISOR_DECISIONS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_OUTCOME_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_PROOF_OF_SERVICE_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_REFUSAL_CLARIFICATION_ADDITIONAL_INFO_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_RESCINDED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_SCANNED_D84_FORM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CO_SWITCHED_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_APPLICANT1_DECLARED_INTENTION_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_APPLICANT2_DECLARED_INTENTION_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_APPLICANT2_SOL_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_FINAL_ORDER_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_FINAL_ORDER_ELIGIBLE_TO_RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_FINAL_ORDER_NO_LONGER_ELIGIBLE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DEEMED_SERVICE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOES_APPLICANT1_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOES_APPLICANT1_WANT_TO_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOES_APPLICANT2_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DOES_APPLICANT2_WANT_TO_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.EXPEDITED_FINAL_ORDER_AUTHORISATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINAL_ORDER_PBA_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINAL_ORDER_SOL_APP1_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINAL_ORDER_SOL_APP2_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINAL_ORDER_SWITCHED_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FRAUD_REFERRAL_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_ADDED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FEE_ACCOUNT_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FEE_ACCOUNT_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FEE_HELP_WITH_FEES_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FEE_PAYMENT_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_FROM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_REFERRAL_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRALS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_DECISION_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_DECISION_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_FEE_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_FEE_PAYMENT_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_FEE_REQUIRED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_FEE_SERVICE_REQUEST_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_FRAUD_CASE_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_JUDGE_OR_LEGAL_ADVISOR_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_URGENT_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_URGENT_CASE_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GRANTED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.HEARING_ATTENDANCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.HOW_TO_RESPOND_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.INTEND_TO_DELAY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.IS_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.JURISDICTION_AGREE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_FINALISE_DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_THE_APPLICANT_2_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_UNION_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_FINAL_ORDER_DETAILS_APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_FINAL_ORDER_DETAILS_SOLE_RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LETTER_PACKS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LOCAL_COURT_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LOCAL_COURT_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_APPLICANT_1_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_APPLICANT_2_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_CERTIFICATE_IS_INCORRECT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_CERTIFY_MARRIAGE_CERTIFICATE_IS_CORRECT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_ISSUE_APPLICATION_WITHOUT_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_MARRIED_IN_UK;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.NEW_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.NOC_WHICH_APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.NOTICE_OF_PROCEEDINGS_SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.OVERDUE_FINAL_ORDER_AUTHORISATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_CASE_PAYMENT_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_APPLICANT_1_NO_PAYMENT_INCLUDED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_APPLICANT_2_NO_PAYMENT_INCLUDED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_APPLICANT_2_PAYMENT_OTHER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_SOLE_OR_APPLICANT_1_PAYMENT_OTHER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PAPER_FORM_SOLE_OR_APPLICANT_1_PAYMENT_OTHER_DETAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REASON_COURTS_ENGLAND_WALES_NO_JURISDICTION_TRANSLATED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REASON_COURTS_HAVE_NO_JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REASON_FAILURE_TO_SERVE_BY_BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.RECEIVED_SERVICE_ADDED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REQUESTS_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.RESPONDENT_WELSH_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SCANNED_D36_FORM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SCANNED_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_ANSWERS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_DOCS_UPLOADED_PRE_SUBMISSION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_FURTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_ACCOUNT_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_ACCOUNT_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_DATE_OF_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_HAS_COMPLETED_ONLINE_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_HELP_WITH_FEES_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_PAYMENT_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_PAYMENT_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_PAYMENT_FEE_SERVICE_REQUEST_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SUCCESSFUL_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.VENUE_OF_HEARING;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.showForState;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {
    private static final String IS_SOLE = "applicationType=\"soleApplication\"";
    private static final String IS_JOINT = "applicationType=\"jointApplication\"";
    private static final String IS_JOINT_AND_HWF_ENTERED =
        "applicationType=\"jointApplication\" AND applicant2HWFReferenceNumber=\"*\"";
    private static final String IS_NEW_PAPER_CASE = "newPaperCase=\"Yes\"";
    private static final String APPLICANTS_CONTACT_DETAILS_PUBLIC =
        "applicant1ContactDetailsType!=\"private\" AND applicant2ContactDetailsType!=\"private\"";
    private static final String APPLICANTS_CONTACT_DETAILS_PRIVATE =
        "applicant1ContactDetailsType=\"private\" OR applicant2ContactDetailsType=\"private\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_PUBLIC = "applicant1ContactDetailsType!=\"private\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_PRIVATE = "applicant1ContactDetailsType=\"private\"";
    private static final String APPLICANT_2_CONTACT_DETAILS_PUBLIC = "applicant2ContactDetailsType!=\"private\"";
    private static final String APPLICANT_2_CONTACT_DETAILS_PRIVATE = "applicant2ContactDetailsType=\"private\"";
    private static final String PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant1NoPaymentIncluded=\"Yes\" AND paperFormSoleOrApplicant1PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant2NoPaymentIncluded=\"Yes\" AND paperFormApplicant2PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_PAYMENT_OTHER_DETAILS =
        String.format("(%s) OR (%s)", PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS, PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS);
    private static final String NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";

    public static final String IS_OVERDUE_AND_APP_1_IS_REPRESENTED_AND_APPLIED = "isFinalOrderOverdue=\"Yes\" AND "
        + "applicant1SolicitorRepresented=\"Yes\" AND doesApplicant1WantToApplyForFinalOrder=\"Yes\"";

    public static final String IS_OVERDUE_AND_APP_2_IS_REPRESENTED_AND_APPLIED = "isFinalOrderOverdue=\"Yes\" AND "
        + "applicant2SolicitorRepresented=\"Yes\" AND doesApplicant2WantToApplyForFinalOrder=\"Yes\"";

    public static final String APPLICANT_2_APPLIED_FOR_FO_FIRST_OR_IS_JOINT = "applicant2AppliedForFinalOrderFirst=\"Yes\" OR " + IS_JOINT;

    public static final String APPLICANT_2_SOL_APPLIED_FOR_FO = "applicant2SolAppliedForFinalOrder=\"Yes\"";

    public static final String APPLICANT_2_SOL_APPLIED_FOR_FO_PBA = "applicant2SolAppliedForFinalOrder=\"Yes\" AND "
        + "applicant2SolPaymentHowToPay=\"feePayByAccount\"";

    public static final String APPLICANT_2_SOL_APPLIED_FOR_FO_HWF = "applicant2SolAppliedForFinalOrder=\"Yes\" AND "
        + "applicant2SolPaymentHowToPay=\"feesHelpWith\"";

    public static final String RESPONDENT_APPLIED_FOR_FO = "applicant2AppliedForFinalOrder=\"Yes\" AND " + IS_SOLE;

    public static final String RESPONDENT_APPLIED_FOR_FO_CARD = """
        applicant2AppliedForFinalOrder=\"Yes\" AND applicant2FinalOrderFeeOrderSummary=\"*\"
        """;

    public static final String RESPONDENT_APPLIED_FOR_FO_HWF = """
        applicant2AppliedForFinalOrder=\"Yes\" AND applicant2FoHWFReferenceNumber=\"*\"
        """;

    private static final String NOTICE_OF_CHANGE_HAS_BEEN_APPLIED = "changeOrganisationRequestField=\"*\" OR nocWhichApplicant=\"*\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        buildWarningsTab(configBuilder);
        buildMatchesTab(configBuilder);
        buildStateTab(configBuilder);
        buildSolicitorAosTab(configBuilder);
        buildInternalUserAosTab(configBuilder);
        buildConditionalOrderTab(configBuilder);
        buildConditionalOrderTabForApp2Sol(configBuilder);
        buildOutcomeOfConditionalOrderTab(configBuilder);
        buildFinalOrderTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildNotesTab(configBuilder);
        buildMarriageCertificateTab(configBuilder);
        buildCivilPartnershipCertificateTab(configBuilder);
        buildServiceApplicationTab(configBuilder);
        buildGeneralReferralTab(configBuilder);
        buildHearingsTab(configBuilder);
        buildGeneralApplicationTab(configBuilder);
        buildLanguageTab(configBuilder);
        buildConfidentialApplicantTab(configBuilder);
        buildConfidentialRespondentTab(configBuilder);
        buildConfidentialApplicant2Tab(configBuilder);
        buildConfidentialDocumentsTab(configBuilder);
        buildCorrespondenceTab(configBuilder);
        buildAmendedApplicationTab(configBuilder);
        buildChangeOfRepresentativeTab(configBuilder);
        buildRequestForInformationTab(configBuilder);
        buildCaseFlagTab(configBuilder);

        // Commented out as requested by service team. This can't be available for super users. Maybe we need a "Developer" role?
        //buildLetterPackTab(configBuilder);
    }

    private void buildWarningsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("transformationAndOcrWarningsTab", "Warnings")
            .showCondition("warnings!=\"\"")
            .field("warnings");
    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .forRoles(APPLICANT_2_SOLICITOR)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    private void buildSolicitorAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        addAosTabFields(configBuilder.tab("aosDetailsSolicitor", "AoS")
            .forRoles(APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
        );
    }

    private void buildInternalUserAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        addAosTabFields(configBuilder.tab("aosDetailsInternal", "AoS").forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER))
            .field("noticeOfProceedingsEmail", "applicant2ContactDetailsType!=\"private\" AND applicant2SolicitorRepresented!=\"Yes\"");
    }

    private Tab.TabBuilder<CaseData, UserRole> addAosTabFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        return tabBuilder
            .showCondition("dateAosSubmitted=\"*\" AND applicationType=\"soleApplication\" AND coSwitchedToSole!=\"Yes\" AND "
                + notShowForState(
                    Draft,
                    AwaitingHWFDecision,
                    AwaitingPayment,
                    Submitted,
                    AwaitingDocuments,
                    AwaitingRequestedInformation,
                    InformationRequested,
                    RequestedInformationSubmitted,
                    AwaitingAos,
                    AosDrafted,
                    AosOverdue,
                    AwaitingService
                )
            )
            .field(APPLICANT_2_OFFLINE, NEVER_SHOW)
            .label("LabelAosTabOnlineResponse-Heading", "applicant2Offline=\"No\"",
                "## This is an online AoS response")
            .label("LabelAosTabOfflineResponse-Heading", "applicant2Offline=\"Yes\"",
                "## This is an offline AoS response")
            .field(CONFIRM_READ_PETITION)
            .field(JURISDICTION_AGREE)
            .field(REASON_COURTS_HAVE_NO_JURISDICTION, "jurisdictionAgree=\"No\"")
            .field(COUNTRY_LIFE_BASED, "jurisdictionAgree=\"No\"")
            .field(INTEND_TO_DELAY)
            .field(APPLICANT_2_LEGAL_PROCEEDINGS)
            .field(APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS)
            .field(APPLICANT_2_LEGAL_PROCEEDINGS_CONCLUDED)
            .field(DUE_DATE)
            .field(HOW_TO_RESPOND_APPLICATION)
            .field(RESPONDENT_WELSH_TRANSLATION)
            .field(APPLICANT_2_SOLICITOR_REPRESENTED)
            .field(APPLICANT_2_SOLICITOR_EMAIL, "applicant2SolicitorRepresented=\"Yes\"")
            .field(NOTICE_OF_PROCEEDINGS_SOLICITOR_FIRM)
            .field(APPLICANT_2_SOLICITOR_REPRESENTED, NEVER_SHOW)
            .field(STATEMENT_OF_TRUTH)
            .field(APPLICANT_2_STATEMENT_OF_TRUTH, "statementOfTruth!=\"*\"")
            .field(DATE_AOS_SUBMITTED)
            .field(AOS_IS_DRAFTED, NEVER_SHOW);
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .label("LabelApplicant1-PaymentHeading", IS_JOINT, "### The applicant")
            .field(APPLICANT_2_HWF_NEED_HELP, NEVER_SHOW)
            .field(APPLICANT_1_HWF,
                "applicationType=\"soleApplication\" OR applicant2HWFReferenceNumber=\"*\"")
            .label("LabelApplicant2-PaymentHeading", IS_JOINT_AND_HWF_ENTERED, "### ${labelContentTheApplicant2UC}")
            .field(APPLICANT_2_HWF, IS_JOINT_AND_HWF_ENTERED)
            .field(NEW_PAPER_CASE, NEVER_SHOW)
            .label("LabelPaperCase-PaymentHeading", IS_NEW_PAPER_CASE, "### Paper Case Payment")
            .field(PAPER_CASE_PAYMENT_METHOD, IS_NEW_PAPER_CASE)
            .field(PAPER_FORM_APPLICANT_1_NO_PAYMENT_INCLUDED, NEVER_SHOW)
            .field(PAPER_FORM_APPLICANT_2_NO_PAYMENT_INCLUDED, NEVER_SHOW)
            .field(PAPER_FORM_SOLE_OR_APPLICANT_1_PAYMENT_OTHER, NEVER_SHOW)
            .field(PAPER_FORM_APPLICANT_2_PAYMENT_OTHER, NEVER_SHOW)
            .label("LabelPaperForm-App1PaymentHeading", PAPER_FORM_PAYMENT_OTHER_DETAILS, "### Paper Form Payment Details")
            .field(PAPER_FORM_SOLE_OR_APPLICANT_1_PAYMENT_OTHER_DETAIL, PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS)
            .field(PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAIL, PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS)
            .label("Applicant2Solicitor-PaymentHeading", APPLICANT_2_SOL_APPLIED_FOR_FO, "### Respondent Solicitor")
            .field(APPLICANT_2_SOL_FINAL_ORDER_FEE_ORDER_SUMMARY, APPLICANT_2_SOL_APPLIED_FOR_FO)
            .field(APPLICANT_2_SOL_PAYMENT_HOW_TO_PAY, APPLICANT_2_SOL_APPLIED_FOR_FO)
            .field(FINAL_ORDER_PBA_NUMBER, APPLICANT_2_SOL_APPLIED_FOR_FO_PBA)
            .field(APPLICANT_2_SOL_FINAL_ORDER_FEE_ACCOUNT_REFERENCE, APPLICANT_2_SOL_APPLIED_FOR_FO)
            .field(APP2_SOL_FO_HWF_REFERENCE_NUMBER, APPLICANT_2_SOL_APPLIED_FOR_FO_HWF)
            .label("Applicant2-PaymentHeading", RESPONDENT_APPLIED_FOR_FO, "### Respondent Final Order")
            .field(APPLICANT_2_FINAL_ORDER_FEE_ORDER_SUMMARY, RESPONDENT_APPLIED_FOR_FO_CARD)
            .field(APPLICANT_2_FO_HWF_NEED_HELP, RESPONDENT_APPLIED_FOR_FO_HWF)
            .field(APPLICANT_2_FO_HWF_REFERENCE_NUMBER, RESPONDENT_APPLIED_FOR_FO_HWF)
            .field(GENERAL_APPLICATION_FEE_ORDER_SUMMARY)
            .field(GENERAL_APPLICATION_FEE_PAYMENT_METHOD)
            .field(GENERAL_APPLICATION_FEE_ACCOUNT_NUMBER)
            .field(GENERAL_APPLICATION_FEE_ACCOUNT_REFERENCE_NUMBER)
            .field(GENERAL_APPLICATION_FEE_HELP_WITH_FEES_REFERENCE_NUMBER)
            .field(CaseData::getPaymentHistoryField);
    }

    private void buildLanguageTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("languageDetails", "Language")
            .label("LabelLanguageDetails-Applicant-Sole", IS_SOLE, "### The applicant")
            .label("LabelLanguageDetails-Applicant-Joint", "applicationType=\"jointApplication\"", "### Applicant 1")
            .field(APPLICANT_WELSH_TRANSLATION)
            .field(APPLICANT_1_USED_WELSH_TRANSLATION_ON_SUBMISSION)
            .field(APPLICANT_1_LEGAL_PROCEEDINGS_DETAILS_TRANSLATED)
            .field(CO_APPLICANT_1_REASON_INFORMATION_NOT_CORRECT_TRANSLATED)
            .field(APPLICANT_1_FINAL_ORDER_LATE_EXPLANATION_TRANSLATED)
            .label("LabelLanguageDetails-Respondent-Sole", IS_SOLE, "### The respondent")
            .label("LabelLanguageDetails-Respondent-Joint", "applicationType=\"jointApplication\"", "### Applicant 2")
            .field(RESPONDENT_WELSH_TRANSLATION)
            .field(APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS_TRANSLATED)
            .field(APPLICANT_2_USED_WELSH_TRANSLATION_ON_SUBMISSION)
            .field(CO_APPLICANT_2_REASON_INFORMATION_NOT_CORRECT_TRANSLATED)
            .field(REASON_COURTS_ENGLAND_WALES_NO_JURISDICTION_TRANSLATED)
            .field(CO_REFUSAL_CLARIFICATION_ADDITIONAL_INFO_TRANSLATED);
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field(CO_CERTIFICATE_OF_ENTITLEMENT_DOCUMENT)
            .field(DOCUMENTS_GENERATED)
            .field(APPLICANT_1_DOCUMENTS_UPLOADED, APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_2_DOCUMENTS_UPLOADED, APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field(CaseData::getGeneralOrders)
            .field(DOCUMENTS_UPLOADED)
            .field(CaseData::getGeneralEmails)
            .field(CERTIFICATE_OF_SERVICE_DOCUMENT)
            .field(CO_PROOF_OF_SERVICE_UPLOAD_DOCUMENTS);
    }

    private void buildRequestForInformationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("requestsForInformation", "Requests For Information")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(REQUESTS_FOR_INFORMATION);
    }

    private void buildCorrespondenceTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("correspondence", "Correspondence")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(CaseData::getGeneralEmails)
            .field(CaseData::getGeneralLetters, APPLICANTS_CONTACT_DETAILS_PUBLIC);
    }

    private void buildConfidentialApplicantTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialApplicant", "Confidential Applicant")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, APPLICANT_1_SOLICITOR, SUPER_USER)
            .showCondition("applicant1ContactDetailsType=\"private\"")
            .field(APPLICANT_1_PHONE_NUMBER)
            .field(APPLICANT_1_EMAIL)
            .field(APPLICANT_1_IN_REFUGE)
            .field(APPLICANT_1_ADDRESS);
    }

    private void buildConfidentialRespondentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialRespondent", "Confidential Respondent")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("applicant2ContactDetailsType=\"private\" AND applicationType=\"soleApplication\"")
            .field(APPLICANT_2_PHONE_NUMBER)
            .field(APPLICANT_2_EMAIL)
            .field(APPLICANT_2_IN_REFUGE)
            .field(APPLICANT_2_ADDRESS);
    }

    private void buildConfidentialApplicant2Tab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialApplicant2", "Confidential Applicant 2")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("applicant2ContactDetailsType=\"private\" AND applicationType=\"jointApplication\"")
            .field(APPLICANT_2_PHONE_NUMBER)
            .field(APPLICANT_2_EMAIL)
            .field(APPLICANT_2_IN_REFUGE)
            .field(APPLICANT_2_ADDRESS);
    }

    private void buildMarriageCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .showCondition("divorceOrDissolution = \"divorce\"")
            .field(LABEL_CONTENT_THE_APPLICANT_2_UC, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP_UC, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(MARRIAGE_APPLICANT_1_NAME)
            .field(MARRIAGE_APPLICANT_2_NAME)
            .field(MARRIAGE_DATE)
            .field(MARRIAGE_MARRIED_IN_UK)
            .field(MARRIAGE_PLACE_OF_MARRIAGE, "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field(MARRIAGE_COUNTRY_OF_MARRIAGE, "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field(MARRIAGE_CERTIFY_MARRIAGE_CERTIFICATE_IS_CORRECT)
            .field(MARRIAGE_CERTIFICATE_IS_INCORRECT_DETAILS, "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field(MARRIAGE_ISSUE_APPLICATION_WITHOUT_CERTIFICATE, "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildCivilPartnershipCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("civilPartnershipDetails", "Civil Partnership Certificate")
            .showCondition("divorceOrDissolution = \"dissolution\"")
            .field(LABEL_CONTENT_THE_APPLICANT_2_UC, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP_UC, "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field(MARRIAGE_APPLICANT_1_NAME)
            .field(MARRIAGE_APPLICANT_2_NAME)
            .field(MARRIAGE_DATE)
            .field(MARRIAGE_MARRIED_IN_UK)
            .field(MARRIAGE_PLACE_OF_MARRIAGE, "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field(MARRIAGE_COUNTRY_OF_MARRIAGE, "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field(MARRIAGE_CERTIFY_MARRIAGE_CERTIFICATE_IS_CORRECT)
            .field(MARRIAGE_CERTIFICATE_IS_INCORRECT_DETAILS, "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field(MARRIAGE_ISSUE_APPLICATION_WITHOUT_CERTIFICATE, "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(CaseData::getNotes);
    }

    private void buildGeneralReferralTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("generalReferral", "General Referral")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(GENERAL_REFERRAL_REASON)
            .field(GENERAL_REFERRAL_URGENT_CASE, "generalReferralReason=\"*\"")
            .field(GENERAL_REFERRAL_URGENT_CASE_REASON, "generalReferralUrgentCase=\"Yes\"")
            .field(FRAUD_REFERRAL_CASE)
            .field(GENERAL_REFERRAL_FRAUD_CASE_REASON, "generalReferralFraudCase=\"Yes\"")
            .field(GENERAL_APPLICATION_FROM, "generalApplicationFrom=\"*\"")
            .field(GENERAL_APPLICATION_REFERRAL_DATE, "generalApplicationReferralDate=\"*\"")
            .field(GENERAL_APPLICATION_ADDED_DATE)
            .field(GENERAL_REFERRAL_TYPE)
            .field(GENERAL_REFERRAL_DOCUMENT)
            .field(GENERAL_REFERRAL_DOCUMENTS)
            .field(ALTERNATIVE_SERVICE_MEDIUM)
            .field(GENERAL_REFERRAL_JUDGE_OR_LEGAL_ADVISOR_DETAILS)
            .field(GENERAL_REFERRAL_FEE_REQUIRED)
            .field(GENERAL_REFERRAL_FEE_METHOD)
            .field(GENERAL_REFERRAL_FEE_SERVICE_REQUEST_REFERENCE)
            .field(GENERAL_REFERRAL_FEE_PAYMENT_REFERENCE)
            .field(GENERAL_REFERRAL_DECISION_DATE)
            .field(GENERAL_REFERRAL_DECISION)
            .field(GENERAL_REFERRAL_DECISION_REASON)
            .field(GENERAL_REFERRALS);
    }

    private void buildHearingsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("hearings", "Hearings")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(DATE_OF_HEARING)
            .field(VENUE_OF_HEARING)
            .field(HEARING_ATTENDANCE);
    }

    private void buildGeneralApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("generalApplication", "General Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition("generalApplications=\"*\"")
            .field(CaseData::getGeneralApplications);
    }

    private void buildConfidentialDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("confidentialDocuments", "Confidential Document")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition(getShowConditionForConfidentialDocumentTab())
            .field(CONFIDENTIAL_DOCUMENTS_GENERATED)
            .field(CONFIDENTIAL_DOCUMENTS_UPLOADED)
            .field(APPLICANT_1_DOCUMENTS_UPLOADED, APPLICANT_1_CONTACT_DETAILS_PRIVATE)
            .field(APPLICANT_2_DOCUMENTS_UPLOADED, APPLICANT_2_CONTACT_DETAILS_PRIVATE)
            .field(SCANNED_DOCUMENTS)
            .field(CaseData::getConfidentialGeneralEmails)
            .field(CaseData::getGeneralLetters, APPLICANTS_CONTACT_DETAILS_PRIVATE);
    }

    private String getShowConditionForConfidentialDocumentTab() {
        return "confidentialDocumentsGenerated=\"*\" "
            + "OR confidentialDocumentsUploaded=\"*\" "
            + "OR (applicant1DocumentsUploaded=\"*\" AND applicant1ContactDetailsType=\"private\") "
            + "OR (applicant2DocumentsUploaded=\"*\" AND applicant2ContactDetailsType=\"private\") "
            + "OR scannedDocuments=\"*\" "
            + "OR confidentialGeneralEmails=\"*\" "
            + "OR (generalLetters=\"*\" AND applicant1ContactDetailsType=\"private\") "
            + "OR (generalLetters=\"*\" AND applicant2ContactDetailsType=\"private\")";
    }

    private void buildServiceApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab("alternativeService", "Service Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER, APPLICANT_1_SOLICITOR)
            .showCondition("receivedServiceApplicationDate=\"*\" OR alternativeServiceOutcomes=\"*\"");
        addServiceApplicationTabFields(tabBuilder);
    }

    private void addServiceApplicationTabFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(RECEIVED_SERVICE_APPLICATION_DATE)
            .field(RECEIVED_SERVICE_ADDED_DATE)
            .field(ALTERNATIVE_SERVICE_TYPE)
            .field(ALTERNATIVE_SERVICE_JUDGE_OR_LEGAL_ADVISOR_DETAILS)
            .field(SERVICE_APPLICATION_ANSWERS)
            .field(SERVICE_APPLICATION_DOCUMENTS, "serviceApplicationDocuments=\"*\"")
            .field(SERVICE_APPLICATION_DOCS_UPLOADED_PRE_SUBMISSION)
            .field(ALTERNATIVE_SERVICE_FEE_REQUIRED)
            .field(SERVICE_PAYMENT_FEE_SERVICE_REQUEST_REFERENCE)
            .field(SERVICE_PAYMENT_FEE_ORDER_SUMMARY)
            .field(SERVICE_PAYMENT_FEE_PAYMENT_REFERENCE)
            .field(SERVICE_PAYMENT_FEE_PAYMENT_METHOD, "servicePaymentFeePaymentMethod=\"*\" AND alternativeServiceFeeRequired=\"Yes\"")
            .field(SERVICE_PAYMENT_FEE_HAS_COMPLETED_ONLINE_PAYMENT)
            .field(SERVICE_PAYMENT_FEE_PAYMENT_REFERENCE)
            .field(SERVICE_PAYMENT_FEE_DATE_OF_PAYMENT,
                "servicePaymentFeePaymentMethod=\"*\" AND alternativeServiceFeeRequired=\"Yes\" OR servicePaymentFeePaymentReference=\"*\"")
            .field(SERVICE_PAYMENT_FEE_ACCOUNT_NUMBER,
                "servicePaymentFeePaymentMethod=\"feePayByAccount\" AND alternativeServiceFeeRequired=\"Yes\"")
            .field(SERVICE_PAYMENT_FEE_ACCOUNT_REFERENCE_NUMBER,
                "servicePaymentFeePaymentMethod=\"feePayByAccount\" AND alternativeServiceFeeRequired=\"Yes\"")
            .field(SERVICE_PAYMENT_FEE_HELP_WITH_FEES_REFERENCE_NUMBER,
                "servicePaymentFeePaymentMethod=\"feePayByHelp\" AND alternativeServiceFeeRequired=\"Yes\"")
            .label("bailiffLocalCourtDetailsLabel",
                "localCourtName=\"*\" OR localCourtEmail=\"*\"", "### Bailiff local court details")
            .field(LOCAL_COURT_NAME)
            .field(LOCAL_COURT_EMAIL)
            .label("bailiffReturnLabel",
                "certificateOfServiceDate=\"*\" OR successfulServedByBailiff=\"*\" OR reasonFailureToServeByBailiff=\"*\"",
                "### Bailiff return")
            .field(CERTIFICATE_OF_SERVICE_DATE)
            .label("serviceOutcomeLabel",
                "serviceApplicationGranted=\"No\" OR serviceApplicationGranted=\"Yes\"",
                "### Outcome of Service Application")
            .field(SERVICE_APPLICATION_GRANTED)
            .field(SERVICE_APPLICATION_DECISION_DATE)
            .field(SERVICE_APPLICATION_FURTHER_DETAILS, "serviceApplicationGranted=\"Yes\"")
            .field(REFUSAL_REASON, "serviceApplicationGranted=\"No\"")
            .field(SERVICE_APPLICATION_REFUSAL_REASON, "serviceApplicationGranted=\"No\"")
            .field(DEEMED_SERVICE_DATE)
            .field(SUCCESSFUL_SERVED_BY_BAILIFF)
            .field(REASON_FAILURE_TO_SERVE_BY_BAILIFF)
            .field(ALTERNATIVE_SERVICE_OUTCOMES);
    }

    private void buildConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab(
            "conditionalOrder", "Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, APPLICANT_1_SOLICITOR, SUPER_USER)
            .showCondition(getShowConditionForConditionalOrderTab());
        addConditionalOrderTabFields(tabBuilder);
    }

    private void buildConditionalOrderTabForApp2Sol(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab(
                "conditionalOrderApp2Sol", "Conditional Order")
            .forRoles(APPLICANT_2_SOLICITOR)
            .showCondition("applicationType=\"jointApplication\" AND ("
                + getShowConditionForConditionalOrderTab() + ")"
            );
        addConditionalOrderTabFields(tabBuilder);
    }

    private String getShowConditionForConditionalOrderTab() {
        return "coApplicant1SubmittedDate=\"*\" OR coApplicant2SubmittedDate=\"*\" OR "
            + showForState(
            ConditionalOrderDrafted,
            ConditionalOrderPending,
            AwaitingLegalAdvisorReferral,
            AwaitingPronouncement,
            JSAwaitingLA,
            SeparationOrderGranted
        );
    }

    private void addConditionalOrderTabFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("labelConditionalOrderDetails-Applicant1",
                "applicationType=\"jointApplication\" AND coApplicant1ApplyForConditionalOrder=\"*\"",
                "### Applicant 1")
            .label("labelApplicant1-SwitchToSole",
                "finalOrderSwitchedToSole=\"Yes\" AND coApplicant1ApplyForConditionalOrder=\"*\"",
                "### Applicant 1")
            .field(LABEL_CONTENT_UNION_TYPE, "applicationType=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, "applicationType=\"NEVER_SHOW\"")
            .field(CO_APPLICANT_1_APPLY_FOR_CONDITIONAL_ORDER)
            .field(CO_APPLICANT_1_CONFIRM_INFORMATION_STILL_CORRECT)
            .field(CO_APPLICANT_1_REASON_INFORMATION_NOT_CORRECT)
            .field(CO_APPLICANT_1_SUBMITTED_DATE)
            .field(CO_APPLICANT_1_CHANGE_OR_ADD_TO_APPLICATION)
            .field(CO_APPLICANT_1_STATEMENT_OF_TRUTH)
            .field(CO_APPLICANT_1_SOLICITOR_NAME)
            .field(CO_APPLICANT_1_SOLICITOR_FIRM)
            .field(CO_APPLICANT_1_SOLICITOR_ADDITIONAL_COMMENTS)
            .label("labelConditionalOrderDetails-Applicant2",
                "applicationType=\"jointApplication\" AND coApplicant2ApplyForConditionalOrder=\"*\"",
                "### Applicant 2")
            .label("labelApplicant2-SwitchToSole",
                "finalOrderSwitchedToSole=\"Yes\" AND coApplicant2ApplyForConditionalOrder=\"*\"",
                "### Applicant 2")
            .field(CO_APPLICANT_2_APPLY_FOR_CONDITIONAL_ORDER)
            .field(CO_APPLICANT_2_CONFIRM_INFORMATION_STILL_CORRECT)
            .field(CO_APPLICANT_2_REASON_INFORMATION_NOT_CORRECT)
            .field(CO_APPLICANT_2_SUBMITTED_DATE)
            .field(CO_APPLICANT_2_CHANGE_OR_ADD_TO_APPLICATION)
            .field(CO_APPLICANT_2_STATEMENT_OF_TRUTH)
            .field(CO_APPLICANT_2_SOLICITOR_NAME)
            .field(CO_APPLICANT_2_SOLICITOR_FIRM)
            .field(CO_APPLICANT_2_SOLICITOR_ADDITIONAL_COMMENTS)
            .field(CO_SCANNED_D84_FORM)
            .field(CO_COURT)
            .field(CO_DATE_AND_TIME_OF_HEARING)
            .field(CO_PRONOUNCEMENT_JUDGE)
            .field(CO_RESCINDED_DATE)
            .field(CO_SWITCHED_TO_SOLE);
    }

    private void buildOutcomeOfConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab(
            "outcomeOfConditionalOrder", "Outcome of Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition(getShowConditionForOutcomeOfConditionalOrderTab());
        addOutcomeOfConditionalOrderTabFields(tabBuilder);
    }

    private String getShowConditionForOutcomeOfConditionalOrderTab() {
        return "coGranted=\"*\" OR "
            + showForState(
            AwaitingAdminClarification,
            AwaitingClarification,
            AwaitingAmendedApplication,
            AwaitingPronouncement,
            ClarificationSubmitted,
            LAReview,
            SeparationOrderGranted
        );
    }

    private void addOutcomeOfConditionalOrderTabFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("labelLegalAdvisorDecision", null, "## Legal advisor decision")
            .field(CO_DECISION_DATE)
            .field(CO_GRANTED)
            .field(CO_CLAIMS_GRANTED)
            .field(CO_CLAIMS_COSTS_ORDER_INFORMATION)
            .field(CO_LEGAL_ADVISOR_DECISIONS)
            .label("labelCoClarificationResponses",
                "coGranted=\"*\" AND coClarificationResponsesSubmitted=\"*\"",
                "## Clarification Responses")
            .field(CO_CLARIFICATION_RESPONSES_SUBMITTED)
            .field(CO_CANNOT_UPLOAD_CLARIFICATION_DOCUMENTS)
            .label("labelCoPronouncementDetails", null, "## Pronouncement Details")
            .field(BULK_LIST_CASE_REFERENCE_LINK)
            .field(CO_COURT)
            .field(CO_DATE_AND_TIME_OF_HEARING)
            .field(CO_PRONOUNCEMENT_JUDGE)
            .field(CO_GRANTED_DATE)
            .field(DATE_FINAL_ORDER_ELIGIBLE_FROM)
            .field(CO_OUTCOME_CASE)
            .label("labelJudgeCostsDecision",
                "coJudgeCostsClaimGranted=\"*\" OR coJudgeCostsOrderAdditionalInfo=\"*\"",
                "## Judge costs decision")
            .field(CO_JUDGE_COSTS_CLAIM_GRANTED)
            .field(CO_JUDGE_COSTS_ORDER_ADDITIONAL_INFO)
            .field(CO_CERTIFICATE_OF_ENTITLEMENT_DOCUMENT)
            .field(CO_CONDITIONAL_ORDER_GRANTED_DOCUMENT)
            .field(CO_RESCINDED_DATE);
    }

    private void buildFinalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("finalOrder", "Final Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("doesApplicant1WantToApplyForFinalOrder=\"Yes\" OR doesApplicant2WantToApplyForFinalOrder=\"Yes\" OR "
                + showForState(
                AwaitingFinalOrder,
                AwaitingFinalOrderPayment,
                AwaitingJointFinalOrder,
                AwaitingGeneralConsideration,
                FinalOrderRequested,
                RespondentFinalOrderRequested,
                FinalOrderPending,
                FinalOrderComplete))
            .label("labelFinalOrderDetails-Applicant1",
                "applicationType=\"jointApplication\"",
                "### Applicant 1")
            .field(IS_FINAL_ORDER_OVERDUE, "doesApplicant1WantToApplyForFinalOrder=\"NEVER_SHOW\"")
            .field(APPLICANT_1_SOLICITOR_REPRESENTED, "doesApplicant1WantToApplyForFinalOrder=\"NEVER_SHOW\"")
            .field(APPLICANT_2_SOLICITOR_REPRESENTED, "doesApplicant1WantToApplyForFinalOrder=\"NEVER_SHOW\"")
            .field(LABEL_CONTENT_FINALISE_DIVORCE_OR_END_CIVIL_PARTNERSHIP, "doesApplicant1WantToApplyForFinalOrder=\"NEVER_SHOW\"")
            .field(DOES_APPLICANT1_WANT_TO_APPLY_FOR_FINAL_ORDER)
            .field(APPLICANT1_FINAL_ORDER_LATE_EXPLANATION)
            .field(APPLICANT1_FINAL_ORDER_STATEMENT_OF_TRUTH)
            .label(FINAL_ORDER_SOL_APP1_STATEMENT_OF_TRUTH, IS_OVERDUE_AND_APP_1_IS_REPRESENTED_AND_APPLIED,
                "The applicant believes that the facts stated in the application are true.")
            .field(APPLICANT1_SOLICITOR_NAME, IS_OVERDUE_AND_APP_1_IS_REPRESENTED_AND_APPLIED)
            .field(APPLICANT1_SOLICITOR_FIRM_NAME, IS_OVERDUE_AND_APP_1_IS_REPRESENTED_AND_APPLIED)
            .field(GRANTED)
            .field(GRANTED_DATE)
            .field(EXPEDITED_FINAL_ORDER_AUTHORISATION)
            .field(OVERDUE_FINAL_ORDER_AUTHORISATION)
            .field(DATE_FINAL_ORDER_NO_LONGER_ELIGIBLE)
            .field(DATE_FINAL_ORDER_ELIGIBLE_TO_RESPONDENT, IS_SOLE)
            .field(DOES_APPLICANT1_INTEND_TO_SWITCH_TO_SOLE)
            .field(DATE_APPLICANT1_DECLARED_INTENTION_TO_SWITCH_TO_SOLE_FO)
            .field(DOES_APPLICANT2_INTEND_TO_SWITCH_TO_SOLE)
            .field(DATE_APPLICANT2_DECLARED_INTENTION_TO_SWITCH_TO_SOLE_FO)
            .field(FINAL_ORDER_SWITCHED_TO_SOLE)
            .label(LABEL_FINAL_ORDER_DETAILS_SOLE_RESPONDENT, RESPONDENT_APPLIED_FOR_FO, "### Respondent")
            .label(LABEL_FINAL_ORDER_DETAILS_APPLICANT2, IS_JOINT, "### Applicant 2")
            .field(APPLICANT2_SOL_APPLIED_FOR_FINAL_ORDER, "applicant2SolAppliedForFinalOrder=\"Yes\"")
            .field(DATE_APPLICANT2_SOL_APPLIED_FOR_FINAL_ORDER, "applicant2SolAppliedForFinalOrder=\"Yes\"")
            .field(APPLICANT2_SOL_FINAL_ORDER_WHY_NEED_TO_APPLY, "applicant2SolAppliedForFinalOrder=\"Yes\"")
            .field(APPLICANT2_SOL_RESPONSIBLE_FOR_FINAL_ORDER, "applicant2SolAppliedForFinalOrder=\"Yes\"")
            .field(APPLICANT2_APPLIED_FOR_FINAL_ORDER_FIRST, NEVER_SHOW)
            .field(LABEL_CONTENT_APPLICANT2, NEVER_SHOW)
            .field(DOES_APPLICANT2_WANT_TO_APPLY_FOR_FINAL_ORDER, APPLICANT_2_APPLIED_FOR_FO_FIRST_OR_IS_JOINT)
            .field(APPLICANT2_APPLIED_FOR_FINAL_ORDER, RESPONDENT_APPLIED_FOR_FO)
            .field(APPLICANT2_FINAL_ORDER_EXPLANATION, APPLICANT_2_APPLIED_FOR_FO_FIRST_OR_IS_JOINT)
            .field(APPLICANT2_FINAL_ORDER_LATE_EXPLANATION, APPLICANT_2_APPLIED_FOR_FO_FIRST_OR_IS_JOINT)
            .field(APPLICANT2_FINAL_ORDER_STATEMENT_OF_TRUTH, APPLICANT_2_APPLIED_FOR_FO_FIRST_OR_IS_JOINT)
            .label(FINAL_ORDER_SOL_APP2_STATEMENT_OF_TRUTH, IS_OVERDUE_AND_APP_2_IS_REPRESENTED_AND_APPLIED,
                "The applicant believes that the facts stated in the application are true.")
            .field(APPLICANT2_SOLICITOR_NAME, IS_OVERDUE_AND_APP_2_IS_REPRESENTED_AND_APPLIED)
            .field(APPLICANT2_SOLICITOR_FIRM_NAME, IS_OVERDUE_AND_APP_2_IS_REPRESENTED_AND_APPLIED)
            .field(SCANNED_D36_FORM);
    }

    private void buildAmendedApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("amendedApplication", "Amended application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition("amendedApplications=\"*\"")
            .field(AMENDED_APPLICATIONS);
    }

    private void buildLetterPackTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("letterPack", "Letter packs")
            .forRoles(SUPER_USER)
            .showCondition("letterPacks=\"*\"")
            .field(LETTER_PACKS);
    }

    private void buildChangeOfRepresentativeTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("changeOfRepresentatives", "Change of representatives")
                .forRoles(CASE_WORKER, SUPER_USER)
            .field(NOC_WHICH_APPLICANT, NEVER_SHOW)
            .field(CHANGE_ORGANISATION_REQUEST_FIELD, NEVER_SHOW)
            .showCondition(NOTICE_OF_CHANGE_HAS_BEEN_APPLIED)
            .field(CHANGE_OF_REPRESENTATIVES);
    }

    private void buildCaseFlagTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseFlags", "Case Flags")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(CaseData::getInternalFlagLauncher, null, "#ARGUMENT(READ)")
            .field(CaseData::getCaseFlags, "internalFlagLauncher = \"ALWAYS_HIDE\"")
            .field(APPLICANT1_FLAGS, "internalFlagLauncher = \"ALWAYS_HIDE\"")
            .field(APPLICANT2_FLAGS, "internalFlagLauncher = \"ALWAYS_HIDE\"")
            .field(APPLICANT1_SOLICITOR_FLAGS, "internalFlagLauncher = \"ALWAYS_HIDE\"")
            .field(APPLICANT2_SOLICITOR_FLAGS, "internalFlagLauncher = \"ALWAYS_HIDE\"");
    }

    private void buildMatchesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("matches", "Matches")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .field(CaseData::getCaseMatches);
    }
}
