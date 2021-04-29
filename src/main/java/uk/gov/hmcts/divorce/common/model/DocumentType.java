package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {
    @JsonProperty("deemedServiceRefused")
    DEEMED_SERVICE_REFUSED("Deemed service refused"),

    @JsonProperty("dispenseWithServiceRefused")
    DISPENSE_WITH_SERVICE_REFUSED("Dispense with service refused"),

    @JsonProperty("generalOrder")
    GENERAL_ORDER("General Order"),

    @JsonProperty("aosOverdueCoverLetter")
    AOS_OVERDUE_COVER_LETTER("AOS Overdue Cover Letter"),

    @JsonProperty("welshTranslation")
    WELSH_TRANSLATION("Welsh Translation"),

    @JsonProperty("deemedAsServiceGranted")
    DEEMED_AS_SERVICE_GRANTED("Deemed as service granted"),

    @JsonProperty("dispenseWithServiceGranted")
    DISPENSE_WITH_SERVICE_GRANTED("Dispense with service granted"),

    @JsonProperty("decreeNisiRefusal")
    DECREE_NISI_REFUSAL("Decree Nisi Refusal - Clarification Response"),

    @JsonProperty("aosOfflineAdulteryFormCoRespondent")
    AOS_OFFLINE_ADULTERY_FORM_CO_RESPONDENT("AOS Offline Adultery Form Co-Respondent"),

    @JsonProperty("aosOfflineAdulteryFormRespondent")
    AOS_OFFLINE_ADULTERY_FORM_RESPONDENT("AOS Offline Adultery Form Respondent"),

    @JsonProperty("aosOfflineUnreasonableBehaviourForm")
    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_FORM("AOS Offline Unreasonable Behaviour / Desertion Form Respondent"),

    @JsonProperty("aosOfflineFiveYearSeparationForm")
    AOS_OFFLINE_FIVE_YEAR_SEPARATION_FORM("AOS Offline Five Year Separation Form Respondent"),

    @JsonProperty("aosOfflineTwoYearSeparationForm")
    AOS_OFFLINE_TWO_YEAR_SEPARATION_FORM("AOS Offline Two Year Separation Form Respondent"),

    @JsonProperty("aosOfflineInvitationLetterCoRespondent")
    AOS_OFFLINE_INVITATION_LETTER_CO_RESPONDENT("AOS Offline Invitation Letter Co-respondent"),

    @JsonProperty("aosOfflineInvitationLetterRespondent")
    AOS_OFFLINE_INVITATION_LETTER_RESPONDENT("AOS Offline Invitation Letter Respondent"),

    @JsonProperty("personalService")
    PERSONAL_SERVICE("Personal Service"),

    @JsonProperty("other")
    OTHER("Other"),

    @JsonProperty("decreeNisiAnswers")
    DECREE_NISI_ANSWERS("Decree Nisi Answers"),

    @JsonProperty("respondentAnswers")
    RESPONDENT_ANSWERS("Respondent Answers"),

    @JsonProperty("petition")
    PETITION("Petition"),

    @JsonProperty("nameChangeEvidence")
    NAME_CHANGE_EVIDENCE("Name change evidence"),

    @JsonProperty("marriageCertificateTranslation")
    MARRIAGE_CERTIFICATE_TRANSLATION("Marriage certificate translation"),

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage certificate"),

    @JsonProperty("email")
    EMAIL("Email"),

    @JsonProperty("D9H")
    D9H("D9H"),

    @JsonProperty("D9D")
    D9D("D9D"),

    @JsonProperty("D84A")
    D84A("D84A"),

    @JsonProperty("D79")
    D79("D79 - Notice of refusal of entitlement to a DN"),

    @JsonProperty("D30")
    D30("D30 - Consideration of applications for DN"),

    @JsonProperty("dispenseWithService")
    DISPENSE_WITH_SERVICE("Dispense with Service"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed Service"),

    @JsonProperty("decreeNisiGranted")
    DECREE_NISI_GRANTED("Decree Nisi Granted"),

    @JsonProperty("decreeNisiApplication")
    DECREE_NISI_APPLICATION("Decree Nisi application (D84/D80)"),

    @JsonProperty("decreeAbsoluteGranted")
    DECREE_ABSOLUTE_GRANTED("Decree Absolute Granted"),

    @JsonProperty("decreeAbsoluteApplication")
    DECREE_ABSOLUTE_APPLICATION("Decree Absolute application"),

    @JsonProperty("costsOrder")
    COSTS_ORDER("Costs Order"),

    @JsonProperty("costs")
    COSTS("Costs"),

    @JsonProperty("correspondence")
    CORRESPONDENCE("Correspondence"),

    @JsonProperty("coRespondentAnswers")
    CO_RESPONDENT_ANSWERS("Co-Respondent Answers"),

    @JsonProperty("certificateOfEntitlement")
    CERTIFICATE_OF_ENTITLEMENT("Certificate Of Entitlement"),

    @JsonProperty("bailiffService")
    BAILIFF_SERVICE("Bailiff Service"),

    @JsonProperty("annexA")
    ANNEX_A("Annex A"),

    @JsonProperty("acknowledgementOfServiceCoRespondent")
    ACKNOWLEDGEMENT_OF_SERVICE_CO_RESPONDENT("Acknowledgement of Service (Co-Respondent)"),

    @JsonProperty("acknowledgementOfService")
    ACKNOWLEDGEMENT_OF_SERVICE("Acknowledgement of Service");

    private final String label;
}
