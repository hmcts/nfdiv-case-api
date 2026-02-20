package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class GeneralApplicationJourneyOptions implements ApplicationAnswers {

    @CCD(
        label = "Can the application be dealt with without a hearing?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationHearingNotRequired",
        searchable = false
    )
    private GeneralApplicationHearingNotRequired hearingNotRequired;

    @CCD(
        label = "Evidence that partner agrees hearing is not required",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        searchable = false
    )
    private List<ListValue<DivorceDocument>> partnerAgreesDocs;

    @CCD(
        label = "Cannot upload evidence that partner agrees hearing is not required",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo cannotUploadAgreedEvidence;

    @CCD(
        label = "Partner details correct",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo partnerDetailsCorrect;

    @CCD(
        label = "What application?",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationType",
        searchable = false
    )
    private GeneralApplicationType type;

    @CCD(
        label = "Specify, what other application?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String typeOtherDetails;

    @CCD(
        label = "Why this application?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String reason;

    @CCD(
        label = "Statement",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String statement;

    @CCD(
        label = "Evidence that partner agrees hearing is not required",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        searchable = false
    )
    private List<ListValue<DivorceDocument>> evidenceDocs;

    @CCD(
        label = "Cannot upload some or all evidence",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo cannotUploadEvidence;
}
