package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralOrderJudge implements HasLabel {
    @JsonProperty("districtJudge")
    DISTRICT_JUDGE("District Judge"),

    @JsonProperty("deputyDistrictJudge")
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),

    @JsonProperty("hisHonourJudge")
    HIS_HONOUR_JUDGE("His Honour Judge"),

    @JsonProperty("herHonourJudge")
    HER_HONOUR_JUDGE("Her Honour Judge"),

    @JsonProperty("recorder")
    RECORDER("Recorder");

    private final String label;
}
