//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.Set;

@ComplexType(
        name = "OrganisationPolicy",
        generate = false
)
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

    @JsonCreator
    public OrganisationPolicy(@JsonProperty("Organisation") Organisation organisation,
                              @JsonProperty("PreviousOrganisations") Set<PreviousOrganisationCollectionItem> previousOrganisations,
                              @JsonProperty("OrgPolicyReference") String orgPolicyReference,
                              @JsonProperty("PrepopulateToUsersOrganisation") YesOrNo prepopulateToUsersOrganisation,
                              @JsonProperty("OrgPolicyCaseAssignedRole") R orgPolicyCaseAssignedRole) {
        this.organisation = organisation;
        this.previousOrganisations = previousOrganisations;
        this.orgPolicyReference = orgPolicyReference;
        this.prepopulateToUsersOrganisation = prepopulateToUsersOrganisation;
        this.orgPolicyCaseAssignedRole = orgPolicyCaseAssignedRole;
    }

    public static <R extends HasRole> OrganisationPolicyBuilder<R> builder() {
        return new OrganisationPolicyBuilder<>();
    }

    public OrganisationPolicy() {
    }

    public Organisation getOrganisation() {
        return this.organisation;
    }

    public static class OrganisationPolicyBuilder<R extends HasRole> {
        private Organisation organisation;
        private Set<PreviousOrganisationCollectionItem> previousOrganisations;
        private String orgPolicyReference;
        private YesOrNo prepopulateToUsersOrganisation;
        private R orgPolicyCaseAssignedRole;

        OrganisationPolicyBuilder() {
        }

        @JsonProperty("Organisation")
        public OrganisationPolicyBuilder<R> organisation(Organisation organisation) {
            this.organisation = organisation;
            return this;
        }

        @JsonProperty("PreviousOrganisations")
        public OrganisationPolicyBuilder<R> previousOrganisations(Set<PreviousOrganisationCollectionItem> previousOrganisations) {
            this.previousOrganisations = previousOrganisations;
            return this;
        }

        @JsonProperty("OrgPolicyReference")
        public OrganisationPolicyBuilder<R> orgPolicyReference(String orgPolicyReference) {
            this.orgPolicyReference = orgPolicyReference;
            return this;
        }

        @JsonProperty("PrepopulateToUsersOrganisation")
        public OrganisationPolicyBuilder<R> prepopulateToUsersOrganisation(YesOrNo prepopulateToUsersOrganisation) {
            this.prepopulateToUsersOrganisation = prepopulateToUsersOrganisation;
            return this;
        }

        @JsonProperty("OrgPolicyCaseAssignedRole")
        public OrganisationPolicyBuilder<R> orgPolicyCaseAssignedRole(R orgPolicyCaseAssignedRole) {
            this.orgPolicyCaseAssignedRole = orgPolicyCaseAssignedRole;
            return this;
        }

        public OrganisationPolicy<R> build() {
            return new OrganisationPolicy<>(this.organisation, this.previousOrganisations, this.orgPolicyReference,
                    this.prepopulateToUsersOrganisation, this.orgPolicyCaseAssignedRole);
        }

        public String toString() {
            return "OrganisationPolicy.OrganisationPolicyBuilder(organisation=" + this.organisation
                    + ", previousOrganisations=" + this.previousOrganisations + ", orgPolicyReference="
                    + this.orgPolicyReference + ", prepopulateToUsersOrganisation=" + this.prepopulateToUsersOrganisation
                    + ", orgPolicyCaseAssignedRole=" + this.orgPolicyCaseAssignedRole + ")";
        }
    }
}
