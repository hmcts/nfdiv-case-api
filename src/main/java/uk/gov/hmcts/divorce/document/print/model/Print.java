package uk.gov.hmcts.divorce.document.print.model;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.List;

@Getter
public class Print {
    private final List<Letter> letters;
    private final String caseId;
    private final String caseRef;
    private final String letterType;
    private final List<String> recipients;
    private final Boolean isInternational;

    public Print(
        final List<Letter> letters,
        final String caseId,
        final String caseRef,
        final String letterType,
        final String recipientName,
        final YesOrNo isInternational
    ) {
        this.letters = letters;
        this.caseId = caseId;
        this.caseRef = caseRef;
        this.letterType = letterType;
        this.recipients = List.of(caseId, recipientName, letterType);
        this.isInternational = null != isInternational && isInternational.toBoolean();
    }

}
