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

import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_JOINT_APPLICANT_1_ANSWERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_SOLE_APPLICANT_1_ANSWERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_DIVORCE_APPLICANT_1_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class DivorceApplicationDraft implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private DraftApplicationTemplateContent draftApplicationTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        log.info("Executing handler for generating draft divorce application for case id {} ", caseId);
        String templateId = caseData.getApplicationType().isSole()
            ? DIVORCE_SOLE_APPLICANT_1_ANSWERS
            : DIVORCE_JOINT_APPLICANT_1_ANSWERS;
        String documentName = caseData.getApplicationType().isSole()
            ? DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME
            : JOINT_DIVORCE_APPLICANT_1_ANSWERS_DOCUMENT_NAME;

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            APPLICATION,
            draftApplicationTemplateContent.apply(caseData, caseId),
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            documentName + caseId
        );

        return caseDetails;
    }
}
