package uk.gov.hmcts.divorce.document.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
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

}
