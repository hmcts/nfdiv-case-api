package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RefusalOption implements HasLabel {

    @JsonProperty("reject")
    REJECT("Reject - request amended petition"),

    @JsonProperty("moreInfo")
    MORE_INFO("Get more information / Evidence"),

    @JsonProperty("adminError")
    ADMIN_ERROR("Admin error / send back to caseworker");

    private final String label;
}
