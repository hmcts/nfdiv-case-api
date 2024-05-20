package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.AddressUK;

import java.time.LocalDateTime;

@Data
@Builder
@ComplexType(
        name = "PreviousOrganisation",
        generate = false
)
public class PreviousOrganisation {
    @JsonProperty("FromTimeStamp")
    @JsonSerialize(
            using = LocalDateTimeSerializer.class
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES
    )
    private LocalDateTime fromTimeStamp;
    @JsonProperty("ToTimeStamp")
    @JsonSerialize(
            using = LocalDateTimeSerializer.class
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES
    )
    private LocalDateTime toTimeStamp;

    @JsonProperty("OrganisationName")
    private String organisationName;

    @JsonProperty("OrganisationAddress")
    private AddressUK organisationAddress;
}
