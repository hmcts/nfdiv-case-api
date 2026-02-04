package uk.gov.hmcts.divorce.noticeofchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChangeOfRepresentationAuthor {
    CASEWORKER_NOTICE_OF_CHANGE("Caseworker notice of change"),
    SOLICITOR_NOTICE_OF_CHANGE("Solicitor notice of change"),
    SOLICITOR_STOP_REPRESENTING_CLIENT("Solicitor stop representing client");

    private final String value;
}
