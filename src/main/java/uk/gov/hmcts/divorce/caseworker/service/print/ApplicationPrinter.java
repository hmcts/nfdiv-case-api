package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class ApplicationPrinter {

    private static final String DIVORCE_APPLICATION_PDF = "application-pdf";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendDivorceApplicationPdf(final CaseData caseData, final Long caseId) {

        final List<Letter> letters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(), APPLICATION);

        Letter divorceApplication = firstElement(letters);

        if (divorceApplication != null) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(Collections.singletonList(divorceApplication),
                caseIdString, caseIdString, DIVORCE_APPLICATION_PDF);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Divorce Application PDF was missing. Expected document with type {} , for Case ID: {}",
                APPLICATION,
                caseId
            );
        }
    }
}
