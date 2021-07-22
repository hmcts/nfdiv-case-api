package uk.gov.hmcts.divorce.systemupdate.service;

public class CcdSearchCaseException extends RuntimeException {

    private static final long serialVersionUID = -1226241000163434323L;

    public CcdSearchCaseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
