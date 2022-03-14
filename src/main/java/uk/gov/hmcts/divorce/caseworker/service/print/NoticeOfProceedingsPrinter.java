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
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;

@Component
@Slf4j
public class NoticeOfProceedingsPrinter {

    private static final String LETTER_TYPE_APPLICANT_1_NOP = "applicant1-notice-of-proceedings";
    private static final String LETTER_TYPE_APPLICANT_2_NOP = "applicant2-notice-of-proceedings";


    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetterToApplicant1(final CaseData caseData, final Long caseId) {

        final List<Letter> letters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(), NOTICE_OF_PROCEEDINGS);

        final boolean isApplicant1Represented = caseData.getApplicant1().isRepresented();
        final boolean isApplicant1Offline = caseData.getApplicant1().isOffline();
        final boolean applicant1NoticeOfProceedingsGenerated = isApplicant1Represented || isApplicant1Offline;

        final boolean isApplicant2Represented = caseData.getApplicant2().isRepresented();
        final boolean isApplicant2Offline = caseData.getApplicant2().isOffline();
        final boolean applicant2NoticeOfProceedingsGenerated = isApplicant2Represented || isApplicant2Offline;

        final Letter noticeOfProceedingsLetter = applicant1NoticeOfProceedingsGenerated && applicant2NoticeOfProceedingsGenerated
            ? letters.get(1)
            : firstElement(letters);

        if (noticeOfProceedingsLetter != null) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(Collections.singletonList(noticeOfProceedingsLetter),
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_1_NOP);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant has missing documents. Expected document with type {} , for Case ID: {}",
                AOS_OVERDUE_LETTER,
                caseId
            );
        }
    }

    public void sendLetterToApplicant2(final CaseData caseData, final Long caseId) {

        final List<Letter> letters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(), NOTICE_OF_PROCEEDINGS);

        final Letter noticeOfProceedingsLetter = firstElement(letters);

        if (noticeOfProceedingsLetter != null) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(Collections.singletonList(noticeOfProceedingsLetter),
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_2_NOP);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant has missing documents. Expected document with type {} , for Case ID: {}",
                AOS_OVERDUE_LETTER,
                caseId
            );
        }
    }
}
