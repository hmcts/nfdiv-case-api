package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CasePaymentRequest {
    private String action;

    private String responsibleParty;
}
