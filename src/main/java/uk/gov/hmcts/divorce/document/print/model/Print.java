package uk.gov.hmcts.divorce.document.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Print {
    private final List<Letter> letters;
    private final String caseId;
    private final String caseRef;
    private final String letterType;

    // TODO: NFDIV-3567 recipients will have a value of applicant/recipient's full name + the letter pack type as a List<String>.
    //  E.g. "recipients": ['John Smith', 'aos-overdue']).
    private final List<String> recipients;
}
