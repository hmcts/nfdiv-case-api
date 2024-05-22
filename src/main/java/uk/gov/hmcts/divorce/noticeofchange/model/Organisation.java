//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(
        name = "Organisation",
        generate = false
)
@Data
public class Organisation {
    @JsonProperty("OrganisationID")
    private String organisationId;
    @JsonProperty("OrganisationName")
    private String organisationName;

    @JsonCreator
    public Organisation(@JsonProperty("OrganisationID") String organisationId, @JsonProperty("OrganisationName") String organisationName) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
    }

    public static OrganisationBuilder builder() {
        return new OrganisationBuilder();
    }

    public Organisation() {
    }

    public static class OrganisationBuilder {
        private String organisationId;
        private String organisationName;

        OrganisationBuilder() {
        }

        @JsonProperty("OrganisationID")
        public OrganisationBuilder organisationId(String organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        @JsonProperty("OrganisationName")
        public OrganisationBuilder organisationName(String organisationName) {
            this.organisationName = organisationName;
            return this;
        }

        public Organisation build() {
            return new Organisation(this.organisationId, this.organisationName);
        }

        public String toString() {
            return "Organisation.OrganisationBuilder(organisationId="
                    + this.organisationId + ", organisationName=" + this.organisationName + ")";
        }
    }
}
