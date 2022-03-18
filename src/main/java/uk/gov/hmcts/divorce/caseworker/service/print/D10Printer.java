package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class D10Printer {

    private static final String LETTER_TYPE_D10_COVERSHEET = "d10-with-coversheet";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    public void printD10WithCoversheet(final CaseData caseData, final Long caseId) {

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
            caseData.getDocumentsGenerated(),
            COVERSHEET);

        final Letter noticeOfProceedingsLetter = firstElement(coversheetLetters);

        final String caseIdString = caseId.toString();
        final Print print = new Print(singletonList(noticeOfProceedingsLetter),
            caseIdString, caseIdString, LETTER_TYPE_D10_COVERSHEET);
        final UUID letterId = bulkPrintService.printAosRespondentPack(print, true);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
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
