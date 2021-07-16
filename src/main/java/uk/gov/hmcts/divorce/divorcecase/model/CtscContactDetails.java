package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CtscContactDetails {
    private String serviceCentre;

    private String centreName;

    private String poBox;

    private String town;

    private String postcode;

    private String emailAddress;

    private String phoneNumber;
}
