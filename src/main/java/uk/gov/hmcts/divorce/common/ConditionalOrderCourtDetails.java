package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionalOrderCourtDetails {

    private String name;

    private String address;

    private String email;

    private String phone;
}
