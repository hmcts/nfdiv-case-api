package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicant2TemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@Slf4j
public class GenerateApplicant2NoticeOfProceedings implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CoversheetApplicant2TemplateContent coversheetApplicant2TemplateContent;

    @Autowired
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Autowired
    private NoticeOfProceedingJointContent jointTemplateContent;

    @Autowired
    private NoticeOfProceedingSolicitorContent solicitorTemplateContent;

    @Autowired
    private CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());

        if (isSoleApplication) {
            generateSoleNoticeOfProceedings(caseData, caseId);
        } else {
            generateJointNoticeOfProceedings(caseData, caseId);
        }

        return caseDetails;
    }

    private void generateSoleNoticeOfProceedings(final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating notice of proceedings for respondent solicitor for case id {} ", caseId);

            var hasSolicitor = caseData.getApplicant2().getSolicitor() != null;
            var hasOrgPolicy = hasSolicitor && caseData.getApplicant2().getSolicitor().getOrganisationPolicy() != null;

            if (hasOrgPolicy) {
                generateNoticeOfProceedings(caseData, caseId, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE);
                if (!caseData.getApplication().isCourtServiceMethod()) {
                    generateCoversheet(
                        caseData,
                        caseId,
                        COVERSHEET_APPLICANT,
                        coversheetApplicant2TemplateContent.apply(caseData, caseId));
                }
            } else {
                generateNoticeOfProceedings(caseData, caseId, NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE);
                generateCoversheet(
                    caseData,
                    caseId,
                    COVERSHEET_APPLICANT2_SOLICITOR,
                    coversheetSolicitorTemplateContent.apply(caseData, caseId));
            }
        } else {
            log.info("Generating notice of proceedings for respondent for sole case id {} ", caseId);

            if (isNotEmpty(caseData.getApplicant2().getEmail())) {
                generateNoticeOfProceedings(caseData, caseId, NFD_NOP_R1_SOLE_APP2_CIT_ONLINE);
                if (!caseData.getApplication().isCourtServiceMethod()) {
                    generateCoversheet(
                        caseData,
                        caseId,
                        COVERSHEET_APPLICANT,
                        coversheetApplicant2TemplateContent.apply(caseData, caseId));
                }
            } else {
                generateNoticeOfProceedings(caseData, caseId, NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE);
                generateCoversheet(
                    caseData,
                    caseId,
                    COVERSHEET_APPLICANT,
                    coversheetApplicant2TemplateContent.apply(caseData, caseId));
            }
        }
    }

    private void generateJointNoticeOfProceedings(final CaseData caseData, final Long caseId) {
        final String templateId;
        final Map<String, Object> content;

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating notice of proceedings for applicant 2 solicitor for case id {} ", caseId);

            content = solicitorTemplateContent.apply(caseData, caseId, false);
            templateId = NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 2 notice of proceedings for applicant for joint case id {} ", caseId);

            content = jointTemplateContent.apply(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());
            templateId = NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_2,
            content,
            caseId,
            templateId,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, now(clock))
        );
    }

    private void generateNoticeOfProceedings(final CaseData caseData, final Long caseId, final String templateId) {
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_2,
            noticeOfProceedingContent.apply(caseData, caseId, caseData.getApplicant1()),
            caseId,
            templateId,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, now(clock))
        );
    }

    private void generateCoversheet(final CaseData caseData,
                                    final Long caseId,
                                    final String templateId,
                                    final Map<String, Object> templateContent) {
        log.info("Generating coversheet for sole case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            COVERSHEET,
            templateContent,
            caseId,
            templateId,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }
}
