package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeemedServiceJourneyOptions implements JourneyOptions {

    @CCD(
            label = "You're about to apply for deemed service",
            access = {DefaultAccess.class}
    )
    private YesOrNo deemedIUnderstand;

    @CCD(
        label = "Provide a statement",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedNoEvidenceStatement;

    @CCD(
        label = "Tell us about your evidence",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedEvidenceDetails;

    @CCD(
        label = "Statement of Truth",
        access = {DefaultAccess.class}
    )
    private YesOrNo deemedStatementOfTruth;

    @Override
    @JsonIgnore
    public boolean citizenWillMakePayment() {
        return YesOrNo.NO.equals(deemedUseHelpWithFees);
    }

    @Override
    @JsonIgnore
    public String citizenHwfReference() {
        return deemedHwfRefNumber;
    }

    @Override
    @JsonIgnore
    public DivorceDocument generateAnswerDocument() {
        return DivorceDocument.builder().documentComment("Example").build();
    }

    @Override
    @JsonIgnore
    public AlternativeServiceType serviceApplicationType() {
        return AlternativeServiceType.DEEMED;
    }
}
