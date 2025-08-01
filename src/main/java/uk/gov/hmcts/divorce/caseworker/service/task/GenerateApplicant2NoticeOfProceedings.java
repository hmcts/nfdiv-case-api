package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingsWithAddressContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP2_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_SOLE_RESPONDENT_CITIZEN;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateApplicant2NoticeOfProceedings implements CaseTask {

    private final CaseDataDocumentService caseDataDocumentService;

    private final CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    private final NoticeOfProceedingContent noticeOfProceedingContent;

    private final NoticeOfProceedingsWithAddressContent noticeOfProceedingsWithAddressContent;

    private final NoticeOfProceedingJointContent jointTemplateContent;

    private final NoticeOfProceedingSolicitorContent solicitorTemplateContent;

    private final NoticeOfProceedingJointJudicialSeparationContent jointContentJudicialSeparationContent;

    private final CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    private final GenerateCoversheet generateCoversheet;

    private final Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        if (isSoleApplication) {
            boolean userAlreadyHasCaseInvite = caseData.getCaseInvite() != null
                && StringUtils.isNotEmpty(caseData.getCaseInvite().accessCode());

            if (!userAlreadyHasCaseInvite) {
                caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());
            }

            if (caseData.isJudicialSeparationCase()) {
                generateSoleJSNoticeOfProceedings(caseData, caseId);
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

    private void generateSoleJSNoticeOfProceedings(CaseData caseData, Long caseId) {
        final Applicant applicant2 = caseData.getApplicant2();
        log.info("Generating NOP for JS respondent for sole case id {} ", caseId);

        var coverSheet = COVERSHEET_APPLICANT;
        var templateId = NFD_NOP_APP2_JS_SOLE;
        var templateContent = noticeOfProceedingContent.apply(caseData, caseId, applicant2,
                caseData.getApplicant2().getLanguagePreference());
        var coversheetContent = coversheetApplicantTemplateContent.apply(caseData, caseId, applicant2);

        if (applicant2.isRepresented()) {
            coverSheet = COVERSHEET_APPLICANT2_SOLICITOR;
            templateId = NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID;
            templateContent = solicitorTemplateContent.apply(caseData, caseId, false);
            coversheetContent = coversheetSolicitorTemplateContent.apply(caseData, caseId);
        }
        generateNoticeOfProceedings(
                caseData,
                caseId,
                templateId,
                templateContent
        );
        log.info("Generating coversheet for JS respondent for sole case id {} ", caseId);
        generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                coverSheet,
                coversheetContent,
                caseData.getApplicant2().getLanguagePreference()
        );
    }

    private void generateSoleNoticeOfProceedings(final CaseData caseData, final Long caseId) {

        final Applicant applicant2 = caseData.getApplicant2();
        if (applicant2.isRepresented()) {
            log.info("Generating notice of proceedings for respondent solicitor for case id {} ", caseId);

            var hasSolicitor = applicant2.getSolicitor() != null;
            var hasOrgId = hasSolicitor && applicant2.getSolicitor().hasOrgId();

            if (hasOrgId) {
                if (!caseData.getApplication().isCourtServiceMethod()) {
                    generateNoticeOfProceedingsWithoutAddress(caseData, caseId, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE);
                    generateCoversheet.generateCoversheet(
                        caseData,
                        caseId,
                        COVERSHEET_APPLICANT2_SOLICITOR,
                        coversheetSolicitorTemplateContent.apply(caseData, caseId),
                        caseData.getApplicant2().getLanguagePreference());
                } else {
                    generateNoticeOfProceedingsWithAddress(caseData, caseId);
                }
            } else {
                generateNoticeOfProceedingsWithoutAddress(caseData, caseId, NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE);
                generateCoversheet.generateCoversheet(
                    caseData,
                    caseId,
                    COVERSHEET_APPLICANT2_SOLICITOR,
                    coversheetSolicitorTemplateContent.apply(caseData, caseId),
                    caseData.getApplicant2().getLanguagePreference());
            }
        } else {
            log.info("Generating notice of proceedings for respondent for sole case id {} ", caseId);

            LanguagePreference applicant2LanguagePreference = applicant2.getLanguagePreference();
            Applicant applicant1 = caseData.getApplicant1();
            boolean isCourtService = caseData.getApplication().isCourtServiceMethod();
            boolean app2BasedOverseas = applicant2.isBasedOverseas() || applicant2.getCorrespondenceAddressIsOverseas() == YesOrNo.YES;
            boolean app2IsOffline = applicant2.isApplicantOffline() || OFFLINE_AOS.equals(caseData.getApplication().getReissueOption());
            final List<Letter> oldCoversheetLetters = getLettersBasedOnContactPrivacy(caseData, COVERSHEET);
            boolean outdatedCoversheetIsPresent = !CollectionUtils.isEmpty(oldCoversheetLetters);

            boolean isCoversheetRequired = !isCourtService || app2BasedOverseas || app2IsOffline || outdatedCoversheetIsPresent;

            generateNoticeOfProceedings(
                    caseData,
                    caseId,
                    NFD_NOP_SOLE_RESPONDENT_CITIZEN,
                    noticeOfProceedingContent.apply(caseData, caseId, applicant1, applicant2LanguagePreference));
            if (isCoversheetRequired) {
                generateCoversheet.generateCoversheet(
                        caseData,
                        caseId,
                        COVERSHEET_APPLICANT,
                        coversheetApplicantTemplateContent.apply(caseData, caseId, caseData.getApplicant2()),
                        caseData.getApplicant2().getLanguagePreference());
            }
        }
    }

    private void generateJointNoticeOfProceedings(final CaseData caseData, final Long caseId) {
        final String templateId;
        final Map<String, Object> templateContent;

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating notice of proceedings for applicant 2 solicitor for case id {} ", caseId);

            templateContent = solicitorTemplateContent.apply(caseData, caseId, false);
            templateId = NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 2 notice of proceedings for applicant for joint case id {} ", caseId);

            templateContent = jointTemplateContent.apply(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());
            templateId = NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }

        generateNoticeOfProceedings(caseData, caseId, templateId, templateContent);
    }

    private void generateJointJSNoticeOfProceedings(CaseData caseData, Long caseId) {

        final String templateId;
        final Map<String, Object> templateContent;

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating applicant 2 solicitor notice of proceedings for joint Judicial Separation case id {} ", caseId);

            templateContent = solicitorTemplateContent.apply(caseData, caseId, false);
            templateId = NFD_NOP_APP1APP2_SOL_JS_JOINT;

            log.info("Generating coversheet for applicant 2 solicitor for joint judicial separation case id {} ", caseId);

            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT2_SOLICITOR,
                coversheetSolicitorTemplateContent.apply(caseData, caseId),
                caseData.getApplicant2().getLanguagePreference(),
                formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, "applicant2", now(clock))
            );

        } else {
            log.info("Generating applicant 2 notice of proceedings for joint Judicial Separation case id {} ", caseId);

            templateContent = jointContentJudicialSeparationContent.apply(caseData, caseId, caseData.getApplicant2(),
                caseData.getApplicant1());
            templateId = NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;

            log.info("Generating coversheet for applicant 2 for joint judicial separation case id {} ", caseId);
            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT,
                coversheetApplicantTemplateContent.apply(caseData, caseId, caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference(),
                formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, "applicant2", now(clock))
            );

        }

        generateNoticeOfProceedings(caseData, caseId, templateId, templateContent);
    }

    private void generateNoticeOfProceedingsWithAddress(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = noticeOfProceedingsWithAddressContent
            .apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference());

        generateNoticeOfProceedings(caseData, caseId, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE, templateContent);
    }

    private void generateNoticeOfProceedingsWithoutAddress(final CaseData caseData,
                                                           final Long caseId,
                                                           final String templateId) {
        generateNoticeOfProceedings(
            caseData,
            caseId,
            templateId,
            noticeOfProceedingContent.apply(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2().getLanguagePreference()));
    }

    private void generateNoticeOfProceedings(final CaseData caseData,
                                             final Long caseId,
                                             final String templateId,
                                             final Map<String, Object> templateContent) {
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_2,
            templateContent,
            caseId,
            templateId,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, now(clock))
        );
    }
}
