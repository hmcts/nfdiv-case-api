package uk.gov.hmcts.divorce.systemupdate.service;

public class ReissueProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1314480900066710790L;

    public ReissueProcessingException(final String message) {
        super(message);
    }
}
