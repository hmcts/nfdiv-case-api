package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class InterimApplicationOptions {

    @JsonUnwrapped
    @CCD(
        label = "No Response Journey Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private NoResponseJourneyOptions noResponseJourneyOptions;

    @JsonUnwrapped
    @CCD(
        label = "Deemed Service Journey Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private DeemedServiceJourneyOptions deemedServiceJourneyOptions;

    @JsonUnwrapped
    @CCD(
        label = "Dispense With Service Journey Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private DispenseWithServiceJourneyOptions dispenseWithServiceJourneyOptions;

    @JsonUnwrapped
    @CCD(
        label = "Bailiff Service Journey Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private BailiffServiceJourneyOptions bailiffServiceJourneyOptions;

    @JsonUnwrapped
    @CCD(
        label = "Alternative Service Journey Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private AlternativeServiceJourneyOptions alternativeServiceJourneyOptions;

    @CCD(
        label = "Active Interim Application Type",
        typeOverride = FixedList,
        typeParameterOverride = "InterimApplicationType",
        access = {DefaultAccess.class},
        searchable = false
    )
    private InterimApplicationType interimApplicationType;

    @CCD(
        label = "You're about to apply for service",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo interimAppsIUnderstand;

    @CCD(
        access = {DefaultAccess.class},
        searchable = false
    )
    private Set<Acknowledgement> agreeToShareDetailsWithRespondentCheckbox;

    @CCD(
        label = "Will you be using Help with Fees for this application?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo interimAppsUseHelpWithFees;

    @CCD(
        label = "Do you have a help with fees reference number?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo interimAppsHaveHwfReference;

    @CCD(
        label = "Are you able to upload evidence?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo interimAppsCanUploadEvidence;

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String interimAppsHwfRefNumber;

    @CCD(
        label = "Upload documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        searchable = false
    )
    private List<ListValue<DivorceDocument>> interimAppsEvidenceDocs;

    @CCD(
        label = "Cannot upload some or all of my documents",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo interimAppsCannotUploadDocs;

    @JsonUnwrapped(prefix = "SearchGovRecords")
    @CCD(
        label = "Search gov records journey options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private SearchGovRecordsJourneyOptions searchGovRecordsJourneyOptions;

    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolicitorPaymentMethod",
        access = {DefaultAccess.class},
        searchable = false
    )
    private SolicitorPaymentMethod interimAppsPaymentMethod;

    @CCD(
        label = "Do you want to Amend your draft service application or withdraw it?",
        hint = "if you withdraw it, you'll be able to start a new service application.",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "DraftServiceApplicationAction"
    )
    private DraftServiceApplicationAction draftServiceApplicationAction;

    @Getter
    @AllArgsConstructor
    public enum Acknowledgement implements HasLabel {

        @JsonProperty("Yes")
        CONFIRM("I understand that the answers may be shared with the respondent");

        private final String label;
    }

    @JsonIgnore
    public ApplicationAnswers getApplicationAnswers() {
        if (interimApplicationType.equals(InterimApplicationType.DEEMED_SERVICE)) {
            return deemedServiceJourneyOptions;
        } else if (interimApplicationType.equals(InterimApplicationType.DISPENSE_WITH_SERVICE)) {
            return dispenseWithServiceJourneyOptions;
        }

        return null;
    }

    @JsonIgnore
    public boolean awaitingDocuments() {
        return YesOrNo.YES.equals(interimAppsCannotUploadDocs);
    }

    @JsonIgnore
    public boolean willMakePayment() {
        return !YesOrNo.YES.equals(interimAppsUseHelpWithFees);
    }
}
