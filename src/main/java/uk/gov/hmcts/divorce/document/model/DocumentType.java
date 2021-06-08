package uk.gov.hmcts.divorce.document.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {

    @JsonProperty("divorceApplication")
    DIVORCE_APPLICATION("Divorce application"),

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage Certificate"),

    @JsonProperty("marriageCertificateTranslation")
    MARRIAGE_CERTIFICATE_TRANSLATION("Marriage Certificate translation"),

    @JsonProperty("nameChangeEvidence")
    NAME_CHANGE_EVIDENCE("Name change evidence"),

    @JsonProperty("costs")
    COSTS("Costs"),

    @JsonProperty("costsOrder")
    COSTS_ORDER("Costs Order"),

    @JsonProperty("serviceSolicitor")
    SERVICE_SOLICITOR("Solicitor service"),

    @JsonProperty("serviceDispensedWith")
    SERVICE_DISPENSED_WITH("Dispense With service "),

    @JsonProperty("serviceDispensedWithGranted")
    SERVICE_DISPENSED_WITH_GRANTED("Dispense With service granted"),

    @JsonProperty("serviceDeemed")
    SERVICE_DEEMED("Deemed service"),

    @JsonProperty("serviceDeemedAsGranted")
    SERVICE_DEEMED_AS_GRANTED("Deemed As service granted"),

    @JsonProperty("serviceBaliff")
    SERVICE_BALIFF("Bailiff Service"),

    @JsonProperty("aosOfflineInvitationLetterToApplicant2")
    AOS_OFFLINE_INVITATION_LETTER_TO_APPLICANT_2("AOS Offline Invitation Letter The Respondent"),

    @JsonProperty("applicant2Answers")
    APPLICANT_2_ANSWERS("The respondent's answers"),

    @JsonProperty("conditionalOrderApplication")
    CONDITIONAL_ORDER_APPLICATION("Conditional Order application"),

    @JsonProperty("conditionalOrderRefusal")
    CONDITIONAL_ORDER_REFUSAL("Conditional Order refusal"),

    @JsonProperty("conditionalOrderRefusalClarificationResponse")
    CONDITIONAL_ORDER_REFUSAL_CLARIFICATION_RESPONSE("Conditional Order refusal - Clarification Response"),

    @JsonProperty("conditionalOrderAnswers")
    CONDITIONAL_ORDER_ANSWERS("Conditional Order answers"),

    @JsonProperty("conditionalOrderCertificateOfEntitlement")
    CONDITIONAL_ORDER_CERTIFICATE_OF_ENTITLEMENT("Certificate of entitlement to Conditional Order"),

    @JsonProperty("conditionalOrderGranted")
    CONDITIONAL_ORDER_GRANTED("Conditional Order granted"),

    @JsonProperty("finalOrderApplication")
    FINAL_ORDER_APPLICATION("Final Order application"),

    @JsonProperty("finalOrderGranted")
    FINAL_ORDER_GRANTED("Final Order granted"),

    @JsonProperty("correspondence")
    CORRESPONDENCE("Correspondence"),

    @JsonProperty("generalApplication")
    GENERAL_APPLICATION("General Application"),

    @JsonProperty("email")
    EMAIL("Email"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
