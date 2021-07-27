package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;

import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;

@Component
@Slf4j
public class MiniApplicationDraft implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private DraftApplicationTemplateContent templateContent;

    @Autowired
    private HttpServletRequest request;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        log.info("Executing handler for generating mini draft for case id {} ", caseId);

        final Supplier<Map<String, Object>> templateContentSupplier = templateContent
            .apply(caseData, caseId, caseDetails.getCreatedDate().toLocalDate());

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            DIVORCE_APPLICATION,
            templateContentSupplier,
            caseId,
            request.getHeader(AUTHORIZATION),
            DIVORCE_MINI_DRAFT_APPLICATION,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
            caseData.getApplicant1().getLanguagePreference()
        );

        return caseDetails;
    }
}
