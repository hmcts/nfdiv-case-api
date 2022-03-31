package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingsPrinter {

    private static final String LETTER_TYPE_APPLICANT_1_NOP = "applicant1-notice-of-proceedings";
    private static final String LETTER_TYPE_APPLICANT_2_NOP = "applicant2-notice-of-proceedings";
    private static final String LETTER_TYPE_APPLICANT_2_SOL_NOP = "applicant2-solicitor-notice-of-proceedings";
    private static final String LETTER_TYPE_APPLICANT_2_SOL_NOP_D10_COVERSHEET = "applicant2-solicitor-notice-of-proceedings-with-d10";
    private static final String LETTER_TYPE_APPLICANT_1_SOL_NOP = "applicant1-solicitor-notice-of-proceedings";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetterToApplicant1(final CaseData caseData, final Long caseId) {

        final List<Letter> lettersWithDocumentTypeNop = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(), NOTICE_OF_PROCEEDINGS_APP_1);

        final Letter noticeOfProceedingsLetter = firstElement(lettersWithDocumentTypeNop);

        final List<Letter> lettersWithDocumentTypeApplication = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(), APPLICATION);

        Letter divorceApplication = firstElement(lettersWithDocumentTypeApplication);

        List<Letter> lettersToPrint = new ArrayList<>();

        if (noticeOfProceedingsLetter != null) {
            lettersToPrint.add(noticeOfProceedingsLetter);
        }

        if (divorceApplication != null) {
            lettersToPrint.add(divorceApplication);
        }

        if (!isEmpty(lettersToPrint)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(lettersToPrint,
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_1_NOP);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant 1 has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_1),
                caseId
            );
        }
    }

    public void sendLetterToApplicant2(final CaseData caseData, final Long caseId) {

        List<Letter> lettersToPrint = lettersForApplicant2(caseData);

        if (!isEmpty(lettersToPrint)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(lettersToPrint,
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_2_NOP);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant 2 has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_2),
                caseId
            );
        }
    }

    public void sendLetterToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        List<Letter> lettersToPrint = lettersForApplicant1(caseData);

        log.info("Sending Notice of Proceedings letter and copy of Divorce Application to applicant solicitor for case: {}", caseId);

        if (!isEmpty(lettersToPrint)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(lettersToPrint,
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_1_SOL_NOP);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant 1 solicitor has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_1),
                caseId
            );
        }
    }

    public void sendLetterToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        List<Letter> lettersToPrint = lettersForApplicant2(caseData);

        if (isNull(caseData.getApplicant2().getSolicitor().getOrganisationPolicy())) {
            log.info("Sending Notice of Proceedings letter, a copy of the Divorce Application "
                + "and a copy of D10 with coversheet to respondent solicitor for case: {}", caseId);
            sendLetterWithD10AndCoversheet(caseData, caseId, lettersToPrint);
        } else {
            log.info("Sending Notice of Proceedings letter and copy of Divorce Application to respondent solicitor for case: {}", caseId);

            if (!isEmpty(lettersToPrint)) {
                final String caseIdString = caseId.toString();
                final Print print = new Print(lettersToPrint,
                    caseIdString, caseIdString, LETTER_TYPE_APPLICANT_2_SOL_NOP);
                final UUID letterId = bulkPrintService.print(print);

                log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
            } else {
                log.warn(
                    "Notice of Proceedings for applicant 2 has missing documents. Expected documents with type {} , for Case ID: {}",
                    List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_2),
                    caseId
                );
            }
        }
    }

    private void sendLetterWithD10AndCoversheet(final CaseData caseData,
                                                final Long caseId,
                                                final List<Letter> lettersToPrint) {

        log.info("Generating coversheet for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            COVERSHEET,
            populateCoversheetContent(caseData, caseId),
            caseId,
            COVERSHEET_APPLICANT2_SOLICITOR,
            caseData.getApplicant2().getLanguagePreference(),
            COVERSHEET_DOCUMENT_NAME
        );

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(), COVERSHEET);

        final Letter coversheet = firstElement(coversheetLetters);

        if (coversheet != null) {
            lettersToPrint.add(0, coversheet);
        }

        if (!isEmpty(lettersToPrint)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(lettersToPrint,
                caseIdString, caseIdString, LETTER_TYPE_APPLICANT_2_SOL_NOP_D10_COVERSHEET);
            final UUID letterId = bulkPrintService.printAosRespondentPack(print, true);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Notice of Proceedings for applicant 2 solicitor has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_2, COVERSHEET),
                caseId
            );
        }
    }

    private List<Letter> lettersForApplicant1(CaseData caseData) {
        return getLettersToPrintForApplicants(caseData, NOTICE_OF_PROCEEDINGS_APP_1);
    }

    private List<Letter> lettersForApplicant2(CaseData caseData) {
        return getLettersToPrintForApplicants(caseData, NOTICE_OF_PROCEEDINGS_APP_2);
    }

    private List<Letter> getLettersToPrintForApplicants(CaseData caseData, DocumentType documentType) {
        final List<Letter> lettersWithDocumentTypeNop = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(), documentType);

        final Letter noticeOfProceedingsLetter = firstElement(lettersWithDocumentTypeNop);

        final List<Letter> lettersWithDocumentTypeApplication = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(), APPLICATION);

        Letter divorceApplication = firstElement(lettersWithDocumentTypeApplication);

        List<Letter> lettersToPrint = new ArrayList<>();

        if (noticeOfProceedingsLetter != null) {
            lettersToPrint.add(noticeOfProceedingsLetter);
        }

        if (divorceApplication != null) {
            lettersToPrint.add(divorceApplication);
        }

        return lettersToPrint;
    }

    private Map<String, Object> populateCoversheetContent(final CaseData caseData,
                                                          final Long caseId) {

        Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateContent.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        return templateContent;
    }
}
