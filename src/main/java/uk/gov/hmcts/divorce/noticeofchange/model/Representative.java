package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Organisation;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
public class Representative {

    @CCD(label = "Name")
    private final String solicitorName;

    @CCD(label = "Email")
    private final String solicitorEmail;

    @CCD(label = "Organisation")
    private final Organisation organisation;
}
