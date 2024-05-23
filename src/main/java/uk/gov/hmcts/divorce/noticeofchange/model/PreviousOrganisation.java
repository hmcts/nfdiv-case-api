//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.AddressUK;

import java.time.LocalDateTime;

@ComplexType(
        name = "PreviousOrganisation",
        generate = false
)
@Data
public class PreviousOrganisation {
    @JsonProperty("FromTimestamp")
    @JsonSerialize(
            using = LocalDateTimeSerializer.class
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    private LocalDateTime fromTimeStamp;
    @JsonProperty("ToTimestamp")
    @JsonSerialize(
            using = LocalDateTimeSerializer.class
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    private LocalDateTime toTimeStamp;
    @JsonProperty("OrganisationName")
    private String organisationName;
    @JsonProperty("OrganisationAddress")
    private AddressUK organisationAddress;

    @JsonCreator
    public PreviousOrganisation(@JsonProperty("FromTimestamp") LocalDateTime fromTimeStamp,
                                @JsonProperty("ToTimestamp") LocalDateTime toTimeStamp,
                                @JsonProperty("OrganisationName") String organisationName,
                                @JsonProperty("OrganisationAddress") AddressUK organisationAddress) {
        this.fromTimeStamp = fromTimeStamp;
        this.toTimeStamp = toTimeStamp;
        this.organisationName = organisationName;
        this.organisationAddress = organisationAddress;
    }

    public static class PreviousOrganisationBuilder {
        private LocalDateTime fromTimeStamp;
        private LocalDateTime toTimeStamp;
        private String organisationName;
        private AddressUK organisationAddress;

        PreviousOrganisationBuilder() {
        }

        @JsonProperty("FromTimestamp")
        @JsonDeserialize(
                using = LocalDateTimeDeserializer.class
        )
        public PreviousOrganisationBuilder fromTimeStamp(LocalDateTime fromTimeStamp) {
            this.fromTimeStamp = fromTimeStamp;
            return this;
        }

        @JsonProperty("ToTimestamp")
        @JsonDeserialize(
                using = LocalDateTimeDeserializer.class
        )
        public PreviousOrganisationBuilder toTimeStamp(LocalDateTime toTimeStamp) {
            this.toTimeStamp = toTimeStamp;
            return this;
        }

        @JsonProperty("OrganisationName")
        public PreviousOrganisationBuilder organisationName(String organisationName) {
            this.organisationName = organisationName;
            return this;
        }

        @JsonProperty("OrganisationAddress")
        public PreviousOrganisationBuilder organisationAddress(AddressUK organisationAddress) {
            this.organisationAddress = organisationAddress;
            return this;
        }

        public PreviousOrganisation build() {
            return new PreviousOrganisation(this.fromTimeStamp, this.toTimeStamp, this.organisationName, this.organisationAddress);
        }

        public String toString() {
            return "PreviousOrganisation.PreviousOrganisationBuilder(fromTimeStamp=" + this.fromTimeStamp
                    + ", toTimeStamp=" + this.toTimeStamp + ", organisationName=" + this.organisationName
                    + ", organisationAddress=" + this.organisationAddress + ")";
        }
    }
}
