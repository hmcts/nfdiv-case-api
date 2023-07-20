package uk.gov.hmcts.divorce.document.print.model;

import lombok.Getter;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
// TODO: NFDIV-3567 - Find all uses of this Print. Constructor now includes a List<String> recipients field. All constructor calls
//  will need to be updated to set this new field.
public class Print {
    private final List<Letter> letters;
    private final String caseId;
    private final String caseRef;
    private final String letterType;

    // TODO: NFDIV-3567 - recipients will have a value of case reference + applicant/recipient's full name + the letter pack type
    //  as a List<String>. E.g. "recipients": ['123412341234', 'John Smith', 'aos-overdue']).
    //  Which is unique per request & repeatable as required.
    private final List<String> recipients;

    public Print(List<Letter> letters, String caseId, String caseRef, String letterType, List<String> recipients) {
        this.letters = letters;
        this.caseId = caseId;
        this.caseRef = caseRef;
        this.letterType = letterType;
        this.recipients = recipients;
    }

    // TODO: NFDIV-3567 - Remove this constructor and use an AllArgsConstructor annotation once you have converted all calls to use new
    //  constructor.
    public Print(List<Letter> letters, String caseId, String caseRef, String letterType) {
        this.letters = letters;
        this.caseId = caseId;
        this.caseRef = caseRef;
        this.letterType = letterType;
        this.recipients = emptyList();
    }
}
