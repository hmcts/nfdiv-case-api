package uk.gov.hmcts.divorce.systemupdate.service;

public class InvalidReissueOptionException extends RuntimeException {

    private static final long serialVersionUID = 1314480900066710790L;

    public InvalidReissueOptionException(final String message) {
        super(message);
    }
}
