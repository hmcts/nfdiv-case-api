package uk.gov.hmcts.divorce.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class PbaResponse {

    private HttpStatus httpStatus;

    private String errorMessage;
}
