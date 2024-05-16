package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(
        name = "Organisation",
        generate = false
)
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

    public String getOrganisationId() {
        return this.organisationId;
    }

    public String getOrganisationName() {
        return this.organisationName;
    }

    @JsonProperty("OrganisationID")
    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    @JsonProperty("OrganisationName")
    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof uk.gov.hmcts.ccd.sdk.type.Organisation)) {
            return false;
        } else {
            Organisation other = (Organisation)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$organisationId = this.getOrganisationId();
                Object other$organisationId = other.getOrganisationId();
                if (this$organisationId == null) {
                    if (other$organisationId != null) {
                        return false;
                    }
                } else if (!this$organisationId.equals(other$organisationId)) {
                    return false;
                }

                Object this$organisationName = this.getOrganisationName();
                Object other$organisationName = other.getOrganisationName();
                if (this$organisationName == null) {
                    if (other$organisationName != null) {
                        return false;
                    }
                } else if (!this$organisationName.equals(other$organisationName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof uk.gov.hmcts.ccd.sdk.type.Organisation;
    }

    public int hashCode() {
        int PRIME = 1;
        int result = 1;
        Object $organisationId = this.getOrganisationId();
        result = result * 59 + ($organisationId == null ? 43 : $organisationId.hashCode());
        Object $organisationName = this.getOrganisationName();
        result = result * 59 + ($organisationName == null ? 43 : $organisationName.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getOrganisationId();
        return "Organisation(organisationId=" + var10000 + ", organisationName=" + this.getOrganisationName() + ")";
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

        public uk.gov.hmcts.ccd.sdk.type.Organisation build() {
            return new uk.gov.hmcts.ccd.sdk.type.Organisation(this.organisationId, this.organisationName);
        }

        public String toString() {
            return "Organisation.OrganisationBuilder(organisationId=" + this.organisationId + ", organisationName=" + this.organisationName + ")";
        }
    }
}
