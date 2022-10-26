package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@Slf4j
public class GenerateCoversheet {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    public void generateCoversheet(final CaseData caseData,
                                   final Long caseId,
                                   final String templateId,
                                   final Map<String, Object> templateContent,
                                   final LanguagePreference languagePreference) {
        log.info("Generating coversheet for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            COVERSHEET,
            templateContent,
            caseId,
            templateId,
            languagePreference,
            formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    public void generateCoversheet(final CaseData caseData,
                                   final Long caseId,
                                   final String templateId,
                                   final Map<String, Object> templateContent,
                                   final LanguagePreference languagePreference,
                                   final String filename) {
        log.info("Generating coversheet {} for case id {} ", filename, caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            COVERSHEET,
            templateContent,
            caseId,
            templateId,
            languagePreference,
            filename
        );
    }
}
