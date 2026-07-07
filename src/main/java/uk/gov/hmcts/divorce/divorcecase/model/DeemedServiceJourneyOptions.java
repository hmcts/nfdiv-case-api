package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)

public class DeemedServiceJourneyOptions implements ApplicationAnswers {

    @CCD(
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String deemedNoEvidenceStatement;

    @CCD(
        label = "Details about your evidence",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String deemedEvidenceDetails;

    private Set<DeemedAcknowledgement> agreeToShareDetailsWithRespondentCheckbox;

    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolicitorPaymentMethod",
        access = {DefaultAccess.class},
        searchable = false
    )
    private SolicitorPaymentMethod deemedPaymentMethod;

    @CCD(
        label = "Applicant 1 uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class},
        searchable = false
    )
    private List<ListValue<DivorceDocument>> deemedEvidenceDocs;

    @Getter
    @AllArgsConstructor
    public enum DeemedAcknowledgement implements HasLabel {

        @JsonProperty("Yes")
        CONFIRM("I understand that the answers may be shared with the respondent");

        private final String label;
    }
}
