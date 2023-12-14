package uk.gov.hmcts.divorce.document.print.model;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@Getter
public class Letter {
    @Deprecated
    private DivorceDocument divorceDocument;
    @Deprecated
    private ConfidentialDivorceDocument confidentialDivorceDocument;
    private Document document;
    private final int numCopiesToPrint;

    @Deprecated
    public Letter(DivorceDocument divorceDocument, int numCopiesToPrint) {
        this.divorceDocument = divorceDocument;
        this.numCopiesToPrint = numCopiesToPrint;
    }

    @Deprecated
    public Letter(ConfidentialDivorceDocument confidentialDivorceDocument, int numCopiesToPrint) {
        this.confidentialDivorceDocument = confidentialDivorceDocument;
        this.numCopiesToPrint = numCopiesToPrint;
    }

    public Letter(Document document, int numCopiesToPrint) {
        this.document = document;
        this.numCopiesToPrint = numCopiesToPrint;
    }

    public Document getDocumentLink() {
        if (this.divorceDocument != null) {
            return this.divorceDocument.getDocumentLink();
        } else if (this.confidentialDivorceDocument != null) {
            return this.confidentialDivorceDocument.getDocumentLink();
        } else {
            return this.document;
        }
    }
}
