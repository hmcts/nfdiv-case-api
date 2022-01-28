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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class NoticeOfChange {

    @CCD(
        label = "Which applicant?",
        access = {CaseworkerAccess.class}
    )
    private WhichApplicant whichApplicant;

    @CCD(
        label = "Are they represented by a solicitor?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo areTheyRepresented;

    @Getter
    @AllArgsConstructor
    public enum WhichApplicant implements HasLabel {
        @JsonProperty("applicant1")
        APPLICANT_1("Applicant 1"),
        @JsonProperty("applicant2")
        APPLICANT_2("Applicant 2");

        private final String label;
    }

}
