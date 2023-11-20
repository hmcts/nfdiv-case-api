package uk.gov.hmcts.divorce.document.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ConfidentialDocumentsReceived implements HasLabel {

    @JsonProperty("aos")
    AOS("Confidential - Acknowledgement of Service"),

    @JsonProperty("annexa")
    ANNEX_A("Confidential - Annex A"),

    @JsonProperty("aosInvitationLetterOfflineResp")
    AOS_INVITATION_LETTER_OFFLINE_RESP("Confidential - AoS offline invitation letter Respondent"),

    @JsonProperty("application")
    APPLICATION("Confidential - Application"),

    @JsonProperty("bailiffService")
    @JsonAlias({"serviceBailiff"})
    BAILIFF_SERVICE("Confidential - Bailiff Service"),

    @JsonProperty("coe")
    COE("Confidential - Certificate Of Entitlement"),

    @JsonProperty("coAnswers")
    CO_ANSWERS("Confidential - Conditional Order Answers"),

    @JsonProperty("conditionalOrderApplication")
    CONDITIONAL_ORDER_APPLICATION("Confidential - Conditional Order Application (D84)"),

    @JsonProperty("conditionalOrderGranted")
    CONDITIONAL_ORDER_GRANTED("Confidential - Conditional Order Granted"),

    @JsonProperty("coRefusalClarificationResp")
    CO_REFUSAL_CLARIFICATION_RESP("Confidential - Conditional Order refusal - clarification response"),

    @JsonProperty("correspondence")
    CORRESPONDENCE("Confidential - Correspondence"),

    @JsonProperty("costs")
    COSTS("Confidential - Costs"),

    @JsonProperty("costsOrder")
    COSTS_ORDER("Confidential - Costs Order"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Confidential - Deemed Service"),

    @JsonProperty("dispenseWithService")
    DISPENSE_WITH_SERVICE("Confidential - Dispense with Service"),

    @JsonProperty("d84a")
    D84A("Confidential - D84A"),

    @JsonProperty("d9d")
    D9D("Confidential - D9D"),

    @JsonProperty("d9h")
    D9H("Confidential - D9H"),

    @JsonProperty("email")
    EMAIL("Confidential - Email"),

    @JsonProperty("finalOrderApplication")
    FINAL_ORDER_APPLICATION("Confidential - Final Order Application"),

    @JsonProperty("finalOrderGranted")
    FINAL_ORDER_GRANTED("Confidential - Final Order Granted"),

    @JsonProperty("generalLetter")
    GENERAL_LETTER("Confidential - General letter"),

    @JsonProperty("aosResponseLetter")
    AOS_RESPONSE_LETTER("Aos response letter"),

    @JsonProperty("marriageCert")
    MARRIAGE_CERT("Confidential - Marriage certificate"),

    @JsonProperty("marriageCertTranslation")
    MARRIAGE_CERT_TRANSLATION("Confidential - Marriage certificate translation"),

    @JsonProperty("nameChange")
    NAME_CHANGE("Confidential - Name change evidence"),

    @JsonProperty("noticeOfRefusalOfEntitlement")
    NOTICE_OF_REFUSAL_OF_ENTITLEMENT("Confidential - Notice of refusal of entitlement to a CO"),

    @JsonProperty("other")
    OTHER("Confidential - Other"),

    @JsonProperty("respondentAnswers")
    RESPONDENT_ANSWERS("Confidential - Respondent Answers"),

    @JsonProperty("solicitorService")
    SOLICITOR_SERVICE("Confidential - Solicitor Service"),

    @JsonProperty("welshTranslation")
    WELSH_TRANSLATION("Confidential - Welsh Translation"),

    @JsonProperty("noticeOfProceedings")
    NOTICE_OF_PROCEEDINGS_APP_1("Notice of proceedings for applicant/applicant 1"),

    @JsonProperty("noticeOfProceedingsApp2")
    NOTICE_OF_PROCEEDINGS_APP_2("Notice of proceedings for respondent/applicant 2"),

    @JsonProperty("certificateOfEntitlementCoverLetter")
    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1("Certificate of entitlement cover letter for applicant/applicant 1"),

    @JsonProperty("certificateOfEntitlementCoverLetterApp2")
    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2("Certificate of entitlement cover letter for respondent/applicant 2"),

    @JsonProperty("coGrantedCoversheet")
    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1("Applicant 1 Conditional Order Granted Coversheet"),

    @JsonProperty("coGrantedCoversheetApp2")
    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2("Applicant 2 Conditional Order Granted Coversheet"),

    @JsonProperty("finalOrderGrantedCoverLetterApp1")
    FINAL_ORDER_GRANTED_COVER_LETTER_APP_1("Applicant 1 Final order granted cover letter"),

    @JsonProperty("conditionalOrderCanApply")
    CONDITIONAL_ORDER_CAN_APPLY("Conditional order can apply"),

    @JsonProperty("finalOrderCanApplyApp1")
    FINAL_ORDER_CAN_APPLY_APP1("Final order can apply - Applicant 1"),

    @JsonProperty("finalOrderCanApplyApp2")
    FINAL_ORDER_CAN_APPLY_APP2("Final order can apply - Applicant 2"),

    @JsonProperty("conditionalOrderReminder")
    CONDITIONAL_ORDER_REMINDER("Conditional order reminder"),

    @JsonProperty("coversheet")
    COVERSHEET("Coversheet"),

    @JsonProperty("finalOrderCanApply")
    FINAL_ORDER_CAN_APPLY("Final order can apply"),

    @JsonProperty("finalOrderGrantedCoverLetterApp2")
    FINAL_ORDER_GRANTED_COVER_LETTER_APP_2("Applicant 2 Final order granted cover letter"),

    @JsonProperty("switchToSoleCoLetter")
    SWITCH_TO_SOLE_CO_LETTER("Switch to Sole Conditional Order Letter"),

    @JsonProperty("aosOverdueLetter")
    AOS_OVERDUE_LETTER("Aos overdue letter"),

    @JsonProperty("judicialSeparationOrderClarificationRefusalCoverLetter")
    JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER(
        "Judicial Separation order clarification refusal cover letter"
    ),

    @JsonProperty("separationOrderClarificationRefusalCoverLetter")
    SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER(
        "Separation order clarification refusal cover letter"
    ),

    @JsonProperty("judicialSeparationOrderRefusalCoverLetter")
    JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER("Judicial Separation order refusal cover letter"),

    @JsonProperty("separationOrderRefusalCoverLetter")
    SEPARATION_ORDER_REFUSAL_COVER_LETTER("Separation order refusal cover letter"),

    @JsonProperty("conditionalOrderRefusalCoverLetter")
    CONDITIONAL_ORDER_REFUSAL_COVER_LETTER("Conditional order refusal cover letter");

    private final String label;
}
