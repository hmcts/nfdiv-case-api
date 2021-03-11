package uk.gov.hmcts.reform.divorce.caseapi.exceptions;

public class UnAuthorisedServiceException extends RuntimeException {
    private static final long serialVersionUID = -2047737262969335485L;

    public UnAuthorisedServiceException(String cause) {
        super(cause);
    }
}
