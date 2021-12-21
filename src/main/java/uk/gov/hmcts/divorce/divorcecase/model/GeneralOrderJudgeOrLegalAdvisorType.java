package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralOrderJudgeOrLegalAdvisorType implements HasLabel {
    @JsonProperty("districtJudge")
    @JsonAlias({"recorder"})
    DISTRICT_JUDGE("District Judge"),

    @JsonProperty("deputyDistrictJudge")
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),

    @JsonProperty("hisHonourJudge")
    HIS_HONOUR_JUDGE("His Honour Judge"),

    @JsonProperty("herHonourJudge")
    HER_HONOUR_JUDGE("Her Honour Judge"),

    @JsonProperty("assistantJusticesClerk")
    ASSISTANT_JUSTICES_CLERK("Assistant Justices Clerk"),

    @JsonProperty("properOfficerOfTheCourt")
    PROPER_OFFICER_OF_THE_COURT("A Proper Officer of the Court");

    private final String label;
}
