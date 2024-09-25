package uk.gov.hmcts.divorce.divorcecase.model;


import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseMatch {

    @CCD(
        label = "Marriage Date",
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @CCD(
        label = "The applicant's full name as on marriage or civil partnership certificate",
        hint = "Exactly as it appears on the certificate. Include any additional text such as 'formally known as'."
    )
    private String applicant1Name;

    @CCD(
        label = "The respondent / applicant2's full name as on marriage or civil partnership certificate",
        hint = "Exactly as it appears on the certificate. Include any additional text such as 'formally known as'."
    )
    private String applicant2Name;

    @CCD(
        label = "Applicant postcode"
    )
    private String applicant1Postcode;

    @CCD(
        label = "Applicant town"
    )
    private String applicant1Town;

    @CCD(
        label = "Respondent / Applicant2 postcode"
    )
    private String applicant2Postcode;

    @CCD(
        label = "Respondent / Applicant2 town"
    )
    private String applicant2Town;

    @CCD(
        label = "Case reference",
        typeOverride = FieldType.CaseLink
    )
    private CaseLink caseLink;

    @CCD(
        label = "Is match valid?"
    )
    private YesOrNo valid;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;  // Identity check
        }
        if (!(o instanceof CaseMatch)) {
            return false;  // Type check
        }

        CaseMatch other = (CaseMatch) o;

        // Check if both CaseLinks and their caseReferences are not null and equal
        return Optional.ofNullable(this.caseLink)
            .map(CaseLink::getCaseReference)
            .equals(Optional.ofNullable(other.caseLink).map(CaseLink::getCaseReference));
    }

    @Override
    public int hashCode() {
        return caseLink != null && caseLink.getCaseReference() != null
            ? caseLink.getCaseReference().hashCode()
            : 0;
    }


}
