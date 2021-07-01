package uk.gov.hmcts.divorce.caseworker.service;

public class CcdSearchCaseException extends RuntimeException {

    private static final long serialVersionUID = -1226241000163434323L;

    public CcdSearchCaseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
