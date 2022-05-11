package uk.gov.hmcts.divorce.document.print.model;

import lombok.Getter;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@Getter
public class Letter {
    private DivorceDocument divorceDocument;
    private ConfidentialDivorceDocument confidentialDivorceDocument;
    private final int count;

    public Letter(DivorceDocument divorceDocument, int count) {
        this.divorceDocument = divorceDocument;
        this.count = count;
    }

    public Letter(ConfidentialDivorceDocument confidentialDivorceDocument, int count) {
        this.confidentialDivorceDocument = confidentialDivorceDocument;
        this.count = count;
    }
}
