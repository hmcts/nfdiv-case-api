package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AL2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_OS_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOLICITOR_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
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
    private NoticeOfProceedingSolicitorContent solicitorContent;

    @Autowired
    private NoticeOfProceedingJointJudicialSeparationContent jointContentJudicialSeparationContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        if (isSoleApplication) {
            if (caseData.isJudicialSeparationCase()) {
                generateSoleNoticeOfProceedingsForJudicialSeparation(caseData, caseId);
            } else {
                generateSoleNoticeOfProceedings(caseData, caseId);
            }
        } else {
            if (caseData.isJudicialSeparationCase()) {
                generateJointJSNoticeOfProceedings(caseData, caseId);
            } else {
                generateJointNoticeOfProceedings(caseData, caseId);
            }
        }

        return caseDetails;
    }

    private void generateSoleNoticeOfProceedings(CaseData caseData, Long caseId) {
        final String templateId;
        final Map<String, Object> content;
        final Applicant applicant1 = caseData.getApplicant1();

        if (applicant1.isRepresented()) {
            log.info("Generating notice of proceedings for applicant solicitor for case id {} ", caseId);

            content = solicitorContent.apply(caseData, caseId, true);

            templateId = caseData.getApplication().isCourtServiceMethod()
                    ? NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS
                    : NFD_NOP_AS2_SOLE_APP1_SOL_SS;
        } else {
            log.info("Generating notice of proceedings for applicant for sole case id {} ", caseId);

            content = templateContent.apply(caseData, caseId, caseData.getApplicant2(), applicant1.getLanguagePreference());
            templateId = caseData.getApplication().isCourtServiceMethod()
                ? NFD_NOP_A1_SOLE_APP1_CIT_CS
                : NFD_NOP_AL2_SOLE_APP1_CIT_PS;
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_1,
            content,
            caseId,
            templateId,
            applicant1.getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
        );
    }

    private void generateJointNoticeOfProceedings(CaseData caseData, Long caseId) {
        String templateId;
        Map<String, Object> content;

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating solicitor applicant 1 notice of proceedings for joint case id {} ", caseId);

            content = solicitorContent.apply(caseData, caseId, true);
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

    private void generateJointJSNoticeOfProceedings(CaseData caseData, Long caseId) {
        String templateId;
        Map<String, Object> content;

        if (caseData.getApplicant1().isRepresented()) {
            log.info("setting applicant 1 solicitor notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            content = solicitorContent.apply(caseData, caseId, true);
            templateId = NFD_NOP_APP1APP2_SOL_JS_JOINT;

        } else {
            log.info("Setting applicant 1 notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            content = jointContentJudicialSeparationContent.apply(caseData, caseId, caseData.getApplicant1(),
                caseData.getApplicant2());
            templateId = NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
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

    private void generateSoleNoticeOfProceedingsForJudicialSeparation(CaseData caseData, Long caseId) {

        final String templateId;
        final Applicant applicant1 = caseData.getApplicant1();
        final LanguagePreference languagePreference = applicant1.getLanguagePreference();
        var isCourtService = caseData.getApplication().isCourtServiceMethod();

        if (applicant1.isRepresented()) {
            log.info("Generating notice of judicial separation proceedings for applicant solicitor for case id {} ", caseId);

            templateId = isCourtService
                    ? NFD_NOP_APP1_SOLICITOR_JS_SOLE
                    : NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_1,
                solicitorContent.apply(caseData, caseId, true),
                caseId,
                templateId,
                languagePreference,
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );
        } else {
            log.info("Generating notice of judicial separation proceedings for applicant for case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_1,
                templateContent.apply(caseData, caseId, caseData.getApplicant2(), languagePreference),
                caseId,
                isCourtService ? NFD_NOP_APP1_JS_SOLE : NFD_NOP_APP1_JS_SOLE_OS_PS,
                languagePreference,
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );
        }
    }
}
