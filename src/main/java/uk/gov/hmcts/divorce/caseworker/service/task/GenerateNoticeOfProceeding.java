package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicant1TemplateContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;

import java.time.Clock;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_JOINT_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_RESP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@Slf4j
public class GenerateNoticeOfProceeding implements CaseTask {

    @Autowired
    private CoversheetApplicant1TemplateContent coversheetTemplateContent;

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

        final boolean isApplicant1Represented = caseData.getApplicant1().isRepresented();
        final boolean isApplicant2Represented = caseData.getApplicant2().isRepresented();

        if (isApplicant1Represented) {
            if (caseData.getApplicant1().getSolicitor().hasOrgId()
                && !caseData.getApplication().isSolicitorServiceMethod()) {
                generateApplicantSolicitorNoticeOfProceedings(caseData, caseId);
            }
        } else {

            String templateId = caseData.getApplicant2().isBasedOverseas()
                ? NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID
                : NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;

            log.info("Generating notice of proceedings for sole case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_1,
                templateContent.apply(caseData, caseId),
                caseId,
                templateId,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );

            if (caseData.getApplication().isPersonalServiceMethod()) {
                log.info("Generating coversheet for case id {} ", caseId);
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    COVERSHEET,
                    coversheetTemplateContent.apply(caseData, caseId),
                    caseId,
                    COVERSHEET_APPLICANT,
                    caseData.getApplicant1().getLanguagePreference(),
                    formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, now(clock))
                );
            }
        }

        if (isApplicant2Represented) {

            log.info("Generating notice of proceedings for respondent on sole case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent.apply(caseData, caseId),
                caseId,
                NOTICE_OF_PROCEEDINGS_RESP_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );

        }
    }

    private void generateJointNoticeOfProceedings(CaseData caseData, Long caseId) {

        final boolean isApplicant1Represented = caseData.getApplicant1().isRepresented();
        final boolean isApplicant1Offline = caseData.getApplicant1().isOffline();

        final boolean isApplicant2Represented = caseData.getApplicant2().isRepresented();

        if (isApplicant1Represented
            && caseData.getApplicant1().getSolicitor().hasOrgId()
            && !caseData.getApplication().isSolicitorServiceMethod()) {
            generateApplicantSolicitorNoticeOfProceedings(caseData, caseId);
        }

        if (!isApplicant1Represented || isApplicant1Offline) {

            log.info("Generating applicant 1 notice of proceedings for joint case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_1,
                jointTemplateContent.apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock)));
        }

        if (isApplicant2Represented) {
            // App2 represented - generate docs for solicitor NOP
            log.info("Generating notice of proceedings(joint) for applicant 2 solicitor for case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                noticeOfProceedingSolicitorContent.apply(caseData, caseId, false),
                caseId,
                NOTICE_OF_PROCEEDINGS_JOINT_SOLICITOR_TEMPLATE_ID,
                caseData.getApplicant2().getLanguagePreference(),
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );
        } else {
            // App2 not represented - generate docs for applicant2 NOP(offline/online)
            log.info("Generating applicant 2 notice of proceedings(joint) for joint case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                jointTemplateContent.apply(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                caseData.getApplicant2().getLanguagePreference(),
                formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock)));
        }
    }

    private void generateApplicantSolicitorNoticeOfProceedings(CaseData caseData, Long caseId) {
        log.info("Generating notice of proceedings for applicant solicitor for case id {} ", caseId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_1,
            noticeOfProceedingSolicitorContent.apply(caseData, caseId, true),
            caseId,
            NOTICE_OF_PROCEEDINGS_JOINT_SOLICITOR_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
        );
    }
}
