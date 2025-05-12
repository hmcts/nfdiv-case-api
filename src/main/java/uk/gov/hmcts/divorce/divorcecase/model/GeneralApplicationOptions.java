package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationOptions {

    @JsonUnwrapped
    @CCD(
        label = "No Response Journey Options",
        access = {DefaultAccess.class}
    )
    private NoResponseJourneyOptions noResponseJourneyOptions;

    @JsonUnwrapped
    @CCD(
        label = "Deemed Service Journey Options",
        access = {DefaultAccess.class}
    )
    private DeemedServiceJourneyOptions deemedServiceJourneyOptions;

    @CCD(
        label = "Active General Application Type",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralApplicationType"
    )
    private GeneralApplicationType generalApplicationType;

    @CCD(
        label = "You're about to apply for service",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsIUnderstand;

    @CCD(
        label = "Will you be using Help with Fees for this application?",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsUseHelpWithFees;

    @CCD(
        label = "Do you have a help with fees reference number?",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsHaveHwfReference;

    @CCD(
        label = "Are you able to upload evidence?",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsCanUploadEvidence;

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = {DefaultAccess.class}
    )
    private String genAppsHwfRefNumber;

    @CCD(
        label = "Upload documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> genAppsEvidenceDocs;

    @CCD(
        label = "Cannot upload some or all of my documents",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsCannotUploadDocs;

    @CCD(
        label = "Statement of Truth",
        access = {DefaultAccess.class}
    )
    private YesOrNo genAppsStatementOfTruth;
}
