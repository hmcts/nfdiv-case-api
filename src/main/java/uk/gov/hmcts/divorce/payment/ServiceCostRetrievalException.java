package uk.gov.hmcts.divorce.payment;

public class ServiceCostRetrievalException extends RuntimeException {
    public ServiceCostRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
