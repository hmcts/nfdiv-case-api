package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterimApplication {
    @JsonUnwrapped(prefix = "options")
    @CCD(
        label = "Service and General Application Options",
        access = {DefaultAccess.class},
        searchable = false
    )
    private InterimApplicationOptions options;
}
