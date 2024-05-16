package uk.gov.hmcts.divorce.noticeofchange.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

@ComplexType(
        name = "OrganisationPolicy",
        generate = false
)
@Builder
@Data
@JsonInclude(Include.NON_NULL)
public class OrganisationPolicy<R extends HasRole> {
    @JsonProperty("Organisation")
    private Organisation organisation;
    @JsonProperty("PreviousOrganisations")
    private Set<PreviousOrganisationCollectionItem> previousOrganisations;
    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;
    @JsonProperty("PrepopulateToUsersOrganisation")
    private YesOrNo prepopulateToUsersOrganisation;
    @JsonProperty("OrgPolicyCaseAssignedRole")
    private R orgPolicyCaseAssignedRole;
}
