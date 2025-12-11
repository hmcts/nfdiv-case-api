package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SearchGovRecordsWhichDepartment implements HasLabel {

    @JsonProperty("dwp")
    DWP("Department for Work and Pensions"),

    @JsonProperty("hmrc")
    HMRC("HM Revenue & Customs"),

    @JsonProperty("other")
    OTHER("Other government departments");

    private final String label;
}
