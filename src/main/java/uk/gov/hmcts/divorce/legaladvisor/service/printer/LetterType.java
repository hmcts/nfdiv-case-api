package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum LetterType {
    AWAITING_AMENDED_APPLICATION_LETTER_TYPE("awaiting-amended-application-letter"),
    AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE("awaiting-clarification-application-letter");

    private final String label;
}
