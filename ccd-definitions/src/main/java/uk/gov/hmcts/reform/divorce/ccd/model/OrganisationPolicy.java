package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.divorce.ccd.access.DefaultAccess;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OrganisationPolicy {

    @CCD(
        label = "Organisation",
        access = { DefaultAccess.class }
    )
    @JsonProperty("Organisation")
    private Organisation organisation;

    @CCD(
        label = "Org Policy Case Assigned Role",
        access = { DefaultAccess.class }
    )
    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @CCD(
        label = "Org Policy Reference",
        access = { DefaultAccess.class }
    )
    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;
}
