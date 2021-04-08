package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ComplexType(name = "OrganisationPolicy")
public class OrganisationPolicy {

    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @Data
    @ComplexType(name = "Organisation")
    public static class Organisation {
        @JsonProperty("OrganisationID")
        private final String organisationID;
    }
}
