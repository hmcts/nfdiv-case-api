package uk.gov.hmcts.divorce.solicitor.client.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.fasterxml.jackson.annotation.Nulls.AS_EMPTY;

@Getter
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindUsersByOrganisationResponse {

    @JsonSetter(nulls = AS_EMPTY)
    private List<ProfessionalUser> users;

    private String organisationIdentifier;
}
