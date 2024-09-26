package uk.gov.hmcts.divorce.document.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {

    @JsonProperty("aosOverdueCoverLetter")
    AOS_OVERDUE_COVER_LETTER("AOS overdue cover letter", true),

    @JsonProperty("acknowledgementOfService")
    @JsonAlias({"acknowledgeOfService", "aos"})
    ACKNOWLEDGEMENT_OF_SERVICE("Acknowledgement of service", false),

    @JsonProperty("amendedApplication")
    AMENDED_APPLICATION("Amended Application", false),

    @JsonProperty("annexA")
    ANNEX_A("Annex A", false),

    @JsonProperty("application")
    @JsonAlias("divorceApplication")
    APPLICATION("Application", false),

    @JsonProperty("appliedForCoLetter")
    APPLIED_FOR_CO_LETTER("Applied for Conditional Order Letter", true),

    @JsonProperty("bailiffCertificateOfService")
    BAILIFF_CERTIFICATE_OF_SERVICE("Bailiff certificate of service", false),

    @JsonProperty("bailiffService")
    @JsonAlias({"serviceBaliff"})
    BAILIFF_SERVICE("Bailiff Service", false),

    @JsonProperty("bailiffServiceRefused")
    BAILIFF_SERVICE_REFUSED("Bailiff Service Refused", false),

    @JsonProperty("certificateOfEntitlementCoverLetter")
    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1("Certificate of entitlement cover letter for applicant/applicant 1", true),

    @JsonProperty("certificateOfEntitlementCoverLetterApp2")
    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2("Certificate of entitlement cover letter for respondent/applicant 2", true),

    @JsonProperty("certificateOfEntitlement")
    CERTIFICATE_OF_ENTITLEMENT("Certificate of entitlement", false),

    @JsonProperty("certificateOfService")
    CERTIFICATE_OF_SERVICE("Certificate of service", false),

    @JsonProperty("conditionalOrderAnswers")
    CONDITIONAL_ORDER_ANSWERS("Conditional order answers", false),

    @JsonProperty("conditionalOrderApplication")
    CONDITIONAL_ORDER_APPLICATION("Conditional order application (D84)", false),

    @JsonProperty("conditionalOrderCanApply")
    CONDITIONAL_ORDER_CAN_APPLY("Conditional order can apply", true),

    @JsonProperty("conditionalOrderReminder")
    CONDITIONAL_ORDER_REMINDER("Conditional order reminder", true),

    @JsonProperty("conditionalOrderGranted")
    CONDITIONAL_ORDER_GRANTED("Conditional Order Granted", false),

    @JsonProperty("coGrantedCoversheet")
    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1("Applicant/Applicant 1 Conditional order granted cover letter", true),

    @JsonProperty("coGrantedCoversheetApp2")
    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2("Respondent/Applicant 2 Conditional order granted cover letter", true),

    @JsonProperty("conditionalOrderRefusal")
    CONDITIONAL_ORDER_REFUSAL("Conditional order refusal - clarification response", false),

    @JsonProperty("conditionalOrderRefusalCoverLetter")
    CONDITIONAL_ORDER_REFUSAL_COVER_LETTER("Conditional order refusal cover letter", true),

    @JsonProperty("judicialSeparationOrderRefusalCoverLetter")
    JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER("Judicial Separation order refusal cover letter", true),

    @JsonProperty("judicialSeparationOrderRefusalSolicitorCoverLetter")
    JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER("Judicial Separation order refusal solicitor cover letter",
        true),

    @JsonProperty("judicialSeparationOrderClarificationRefusalCoverLetter")
    JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER("Judicial Separation order clarification refusal cover letter",
        true),

    @JsonProperty("judicialSeparationOrderClarificationRefusalSolicitorCoverLetter")
    JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER(
      "Judicial Separation order clarification refusal solicitor cover letter", true
    ),

    @JsonProperty("separationOrderRefusalCoverLetter")
    SEPARATION_ORDER_REFUSAL_COVER_LETTER("Separation order refusal cover letter", true),

    @JsonProperty("separationOrderRefusalSolicitorCoverLetter")
    SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER(
        "Separation order refusal solicitor cover letter", true
    ),

    @JsonProperty("separationOrderClarificationRefusalCoverLetter")
    SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER(
        "Separation order clarification refusal cover letter", true
    ),

    @JsonProperty("separationOrderClarificationRefusalSolicitorCoverLetter")
    SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER(
        "Separation order clarification refusal solicitor cover letter", true
    ),

    @JsonProperty("correspondence")
    CORRESPONDENCE("Correspondence", true),

    @JsonProperty("costs")
    COSTS("Costs", false),

    @JsonProperty("costsOrder")
    COSTS_ORDER("Costs order", false),

    @JsonProperty("coversheet")
    COVERSHEET("Coversheet", true),

    @JsonProperty("d84")
    D84("D84", false),

    @JsonProperty("d36")
    D36("D36", false),

    @JsonProperty("d9D")
    D9D("D9D", false),

    @JsonProperty("d9H")
    D9H("D9H", false),

    @JsonProperty("d10")
    D10("D10", false),

    @JsonProperty("d11")
    D11("D11", false),

    @JsonProperty("deemedService")
    @JsonAlias({"serviceDeemed"})
    DEEMED_SERVICE("Deemed service", false),

    @JsonProperty("deemedAsServiceGranted")
    DEEMED_AS_SERVICE_GRANTED("Deemed as service granted", false),

    @JsonProperty("deemedServiceRefused")
    DEEMED_SERVICE_REFUSED("Deemed service refused", false),

    @JsonProperty("dispenseWithService")
    @JsonAlias({"serviceDispensedWith"})
    DISPENSE_WITH_SERVICE("Dispense with service", false),

    @JsonProperty("dispenseWithServiceGranted")
    @JsonAlias({"serviceDispensedWithGranted"})
    DISPENSE_WITH_SERVICE_GRANTED("Dispense with service granted", false),

    @JsonProperty("dispenseWithServiceRefused")
    DISPENSE_WITH_SERVICE_REFUSED("Dispense with service refused", false),

    @JsonProperty("email")
    EMAIL("Email", false/*like generalLetter this is handled separately*/),

    @JsonProperty("finalOrderCanApply")
    FINAL_ORDER_CAN_APPLY("Final order can apply", true),

    @JsonProperty("finalOrderCanApplyApp1")
    FINAL_ORDER_CAN_APPLY_APP1("Final order can apply - Applicant 1", true),

    @JsonProperty("finalOrderCanApplyApp2")
    FINAL_ORDER_CAN_APPLY_APP2("Final order can apply - Applicant 2", true),

    @JsonProperty("finalOrderCanApplyRespondent")
    FINAL_ORDER_CAN_APPLY_RESPONDENT("Final order can apply - Respondent", true),

    @JsonProperty("finalOrderApplication")
    FINAL_ORDER_APPLICATION("Final Order application (D36)", false),

    @JsonProperty("finalOrderGranted")
    FINAL_ORDER_GRANTED("Final order granted", false),

    @JsonProperty("finalOrderGrantedCoverLetterApp1")
    FINAL_ORDER_GRANTED_COVER_LETTER_APP_1("Applicant/Applicant 1 Final order granted cover letter", true),

    @JsonProperty("finalOrderGrantedCoverLetterApp2")
    FINAL_ORDER_GRANTED_COVER_LETTER_APP_2("Respondent/Applicant 2 Final order granted cover letter", true),

    @JsonProperty("generalOrder")
    GENERAL_ORDER("General order", false),

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage/Civil Partnership Certificate", false),

    @JsonProperty("marriageCertificateTranslation")
    MARRIAGE_CERTIFICATE_TRANSLATION("Marriage/Civil Partnership Certificate translation", false),

    @JsonProperty("nameChangeEvidence")
    NAME_CHANGE_EVIDENCE("Name change evidence", false),

    @JsonProperty("noticeOfProceedings")
    NOTICE_OF_PROCEEDINGS_APP_1("Notice of proceedings for applicant/applicant 1", false /*handled separately*/),

    @JsonProperty("noticeOfProceedingsApp2")
    NOTICE_OF_PROCEEDINGS_APP_2("Notice of proceedings for respondent/applicant 2", false /*handled separately*/),

    @JsonProperty("noticeOfRefusalOfEntitlement")
    NOTICE_OF_REFUSAL_OF_ENTITLEMENT("Notice of refusal of entitlement to a CO", true),

    @JsonProperty("objectionToCosts")
    OBJECTION_TO_COSTS("Objection to costs", false),

    @JsonProperty("other")
    @JsonAlias({"aosOfflineInvitationLetterToApplicant2"})
    OTHER("Other", true),

    @JsonProperty("pronouncementList")
    PRONOUNCEMENT_LIST("Pronouncement List", false),

    @JsonProperty("respondentAnswers")
    @JsonAlias("applicant2Answers")
    RESPONDENT_ANSWERS("Respondent answers", false),

    @JsonProperty("aos")
    @Deprecated
    RESPONDENT_INVITATION("Respondent Invitation", false),

    @JsonProperty("solicitorService")
    @JsonAlias({"serviceSolicitor"})
    SOLICITOR_SERVICE("Solicitor Service", false),

    @JsonProperty("welshTranslation")
    WELSH_TRANSLATION("Welsh Translation", false),

    @JsonProperty("aosResponseLetter")
    AOS_RESPONSE_LETTER("Aos response letter", true),

    @JsonProperty("aosOverdueLetter")
    AOS_OVERDUE_LETTER("Aos overdue letter", true),

    @JsonProperty("generalLetter")
    GENERAL_LETTER("General letter", true),

    @JsonProperty("generalApplication")
    GENERAL_APPLICATION("General application", false),

    @JsonProperty("switchToSoleCoLetter")
    SWITCH_TO_SOLE_CO_LETTER("Switch to Sole Conditional Order Letter", true),

    @JsonProperty("grantOfRepresentation")
    GRANT_OF_REPRESENTATION("NoC grant of representation letter", true);

    private final String label;
    private final boolean potentiallyConfidential;
}
