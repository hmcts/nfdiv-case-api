package uk.gov.hmcts.divorce.document.model;

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

    @JsonProperty("baliffService")
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
    WELSH_TRANSLATION("Confidential - Welsh Translation");

    private final String label;
}
