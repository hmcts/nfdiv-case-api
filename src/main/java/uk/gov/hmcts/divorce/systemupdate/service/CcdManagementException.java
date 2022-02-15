package uk.gov.hmcts.divorce.systemupdate.service;

import lombok.Getter;

@Getter
public class CcdManagementException extends RuntimeException {

    private static final long serialVersionUID = 7645179305993636259L;

    private int status;

    public CcdManagementException(final int status, final String message, final Throwable throwable) {
        super(message, throwable);
        this.status = status;
    }
}
