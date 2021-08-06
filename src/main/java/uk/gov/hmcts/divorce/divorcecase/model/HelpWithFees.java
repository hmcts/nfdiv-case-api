package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class HelpWithFees {

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private String referenceNumber;

    @CCD(
        label = "Need help with fees?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo needHelp;

    @CCD(
        label = "Applied for help with fees?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo appliedForFees;

}
