package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.print.BulkPrintService;
import uk.gov.hmcts.divorce.print.model.Letter;
import uk.gov.hmcts.divorce.print.model.Print;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersOfDocumentTypes;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Component
@Slf4j
public class AosPackPrinter {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final List<DocumentType> AOS_DOCUMENT_TYPES = asList(
        DIVORCE_APPLICATION,
        DOCUMENT_TYPE_RESPONDENT_INVITATION);

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId) {

        final List<Letter> aosLetters = lettersOfDocumentTypes(
            caseData.getDocumentsGenerated(),
            AOS_DOCUMENT_TYPES);

        if (aosLetters.size() == AOS_DOCUMENT_TYPES.size()) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(aosLetters, caseIdString, caseIdString, LETTER_TYPE_RESPONDENT_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AOS Pack print has missing or incorrect documents. Expected 2 but has {} documents, for Case ID: {}",
                aosLetters.size(),
                caseId);
        }
    }
}
