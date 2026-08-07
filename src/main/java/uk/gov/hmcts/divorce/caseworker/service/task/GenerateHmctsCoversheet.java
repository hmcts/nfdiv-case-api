package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_HMCTS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.HMCTS_COVERSHEET_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.HMCTS_COVERSHEET;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateHmctsCoversheet {

    private final CaseDataDocumentService caseDataDocumentService;

    private final Clock clock;

    private final DocmosisCommonContent docmosisCommonContent;

    public void addToDocumentsGenerated(final CaseDetails<CaseData, State> details) {
        if (hasHmctsCoverSheet(details.getData())) {
            log.info("HMCTS coversheet already exists for case: {}. Skipping.", details.getId());
        } else {
            log.info("Generating HMCTS coversheet for case id {} ", details.getId());
            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                details.getData(),
                HMCTS_COVERSHEET,
                getTemplateContent(details.getId()),
                details.getId(),
                COVERSHEET_HMCTS,
                LanguagePreference.ENGLISH,
                formatDocumentName(details.getId(), HMCTS_COVERSHEET_NAME, now(clock))
            );
        }
    }

    private Map<String, Object> getTemplateContent(final Long caseId) {
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(LanguagePreference.ENGLISH);
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        return templateContent;
    }

    public boolean hasHmctsCoverSheet(final CaseData caseData) {
        final List<ListValue<DivorceDocument>> documentsGenerated = caseData.getDocuments().getDocumentsGenerated();
        return documentsGenerated != null && documentsGenerated.stream().anyMatch(
            doc -> doc.getValue().getDocumentType().equals(HMCTS_COVERSHEET)
        );
    }
}
