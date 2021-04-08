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
public class Organisation {

    @CCD(
        label = "Organisation Id",
        access = { DefaultAccess.class }
    )
    @JsonProperty("OrganisationID")
    private String organisationID;

    @CCD(
        label = "Organisation name",
        access = { DefaultAccess.class }
    )
    @JsonProperty("OrganisationName")
    private String organisationName;
}
