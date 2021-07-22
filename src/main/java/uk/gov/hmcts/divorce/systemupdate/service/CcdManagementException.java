package uk.gov.hmcts.divorce.systemupdate.service;

import lombok.Getter;

@Getter
public class CcdManagementException extends RuntimeException {

    private static final long serialVersionUID = 2226978877584947432L;

    public CcdManagementException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
