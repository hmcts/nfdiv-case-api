package uk.gov.hmcts.divorce.document.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

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

//    public Print(List<Letter> letters, String caseId, String caseRef, String letterType, List<String> recipients) {
//        this.letters = letters;
//        this.caseId = caseId;
//        this.caseRef = caseRef;
//        this.letterType = letterType;
//        this.recipients = recipients;
//    }
//
//    // TODO: NFDIV-3567 - Remove this constructor and use an AllArgsConstructor annotation once you have converted all calls to use new
//    //  constructor.
//    public Print(List<Letter> letters, String caseId, String caseRef, String letterType) {
//        this.letters = letters;
//        this.caseId = caseId;
//        this.caseRef = caseRef;
//        this.letterType = letterType;
//
//        // TODO: NFDIV-3567 - Setting recipients to a random string to avoid duplicate requests while branch is being worked on.
//        //  As an empty array would be marked as duplicate.
//        byte[] array = new byte[7];
//        new Random().nextBytes(array);
//        String generatedString = new String(array, StandardCharsets.UTF_8);
//
//        this.recipients = List.of(generatedString);
//    }
}
