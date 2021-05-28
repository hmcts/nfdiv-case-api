package uk.gov.hmcts.divorce.solicitor.client.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationsResponse {
    @JsonProperty(value = "contactInformation")
    private List<OrganisationContactInformation> contactInformation;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "organisationIdentifier")
    private String organisationIdentifier;
}
