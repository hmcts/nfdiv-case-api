package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class CreateServiceRequestBody {

    private Long caseReference;

    private CasePaymentRequest casePaymentRequest;

    private String callBackUrl;

    private Long ccdCaseNumber;

    private String hmctsOrgId;

    private List<PaymentItem> fees;
}
