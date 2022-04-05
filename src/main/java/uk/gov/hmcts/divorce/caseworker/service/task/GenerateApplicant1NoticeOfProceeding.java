package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;

@Component
@Slf4j
public class GenerateApplicant1NoticeOfProceeding implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private NoticeOfProceedingContent templateContent;

    @Autowired
    private NoticeOfProceedingJointContent jointTemplateContent;

    @Autowired
    private NoticeOfProceedingSolicitorContent noticeOfProceedingSolicitorContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        if (isSoleApplication) {
            generateSoleNoticeOfProceedings(caseData, caseId);
        } else {
            generateJointNoticeOfProceedings(caseData, caseId);
        }

        return caseDetails;
    }

    private void generateSoleNoticeOfProceedings(CaseData caseData, Long caseId) {
        String templateId;
        Map<String, Object> content;

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating notice of proceedings for applicant solicitor for case id {} ", caseId);

            content = noticeOfProceedingSolicitorContent.apply(caseData, caseId, true);
            templateId = caseData.getApplication().isCourtServiceMethod()
                ? NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS
                : NFD_NOP_AS2_SOLE_APP1_SOL_SS;
        } else {
            log.info("Generating notice of proceedings for applicant for sole case id {} ", caseId);

            content = templateContent.apply(caseData, caseId);
            templateId = caseData.getApplication().isCourtServiceMethod()
                ? NFD_NOP_A1_SOLE_APP1_CIT_CS
                : NFD_NOP_A2_SOLE_APP1_CIT_PS;
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_1,
            content,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
        );
    }

    private void generateJointNoticeOfProceedings(CaseData caseData, Long caseId) {
        String templateId;
        Map<String, Object> content;

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating solicitor applicant 1 notice of proceedings for joint case id {} ", caseId);

            content = noticeOfProceedingSolicitorContent.apply(caseData, caseId, true);
            templateId = NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 1 notice of proceedings for joint case id {} ", caseId);

            content = jointTemplateContent.apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
            templateId = NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_1,
            content,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
        );
    }

}
