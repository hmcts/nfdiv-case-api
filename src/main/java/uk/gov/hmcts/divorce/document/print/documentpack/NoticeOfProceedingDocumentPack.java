package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_JOINT_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SOLE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AL2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_OS_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOLICITOR_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP2_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeOfProceedingDocumentPack implements DocumentPack {

    private String letterTypePack;

    @Override
    public String getLetterId() {
        return letterTypePack;
    }

    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {

        var isApplicant1 = applicant.equals(caseData.getApplicant1());
        var caseId = Long.parseLong(caseData.getHyphenatedCaseRef().replace("-", ""));
        var isCourtService = caseData.getApplication().isCourtServiceMethod();
        var isJudicialSeparationCase = caseData.isJudicialSeparationCase();
        var isSoleApplication = caseData.getApplicationType().isSole();
        var applicationTemplateId = getApplicationTemplateId(isSoleApplication, isJudicialSeparationCase);
        var coversheet = getCoverSheetTemplateId(applicant);
        var applicant1NoticeOfProceedingTemplateId = getNoticeOfProceedingTemplateId(caseData, isCourtService,
                isJudicialSeparationCase, caseId, true);
        var applicant2NoticeOfProceedingTemplateId = getNoticeOfProceedingTemplateId(caseData, isCourtService,
                isJudicialSeparationCase, caseId, false);

        var applicationDocumentName = isJudicialSeparationCase ? JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME
                : DIVORCE_APPLICATION_DOCUMENT_NAME;

        DocumentPackInfo documentPackInfo = null;

        if (isApplicant1) {
            letterTypePack = "applicant-aos-pack";
            if (isCourtService) {
                log.info("Sending AOS pack to applicant for sole case with id: {}", caseId);

                documentPackInfo = new DocumentPackInfo(
                        ImmutableMap.of(
                                NOTICE_OF_PROCEEDINGS_APP_1, Optional.of(applicant1NoticeOfProceedingTemplateId),
                                APPLICATION, Optional.of(applicationTemplateId)
                        ),
                        ImmutableMap.of(
                                applicant1NoticeOfProceedingTemplateId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                                applicationTemplateId, applicationDocumentName
                        ));
            } else if (caseData.getApplication().isPersonalServiceMethod()) {
                log.info("Bulk printing NOP and application pack. Case id: {}:", caseId);

                documentPackInfo = new DocumentPackInfo(
                        ImmutableMap.of(
                        NOTICE_OF_PROCEEDINGS_APP_1, Optional.of(applicant1NoticeOfProceedingTemplateId),
                        APPLICATION, Optional.of(applicationTemplateId),
                        COVERSHEET, Optional.of(coversheet),
                        NOTICE_OF_PROCEEDINGS_APP_2, Optional.of(applicant2NoticeOfProceedingTemplateId)
                ),
                        ImmutableMap.of(
                                applicant1NoticeOfProceedingTemplateId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                                applicationTemplateId, applicationDocumentName,
                                coversheet, COVERSHEET_DOCUMENT_NAME,
                                applicant2NoticeOfProceedingTemplateId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME
                        ));
            }
        } else {
            letterTypePack = "respondent-aos-pack";

            if (applicant.isRepresented()) {

                return getDocumentPackInfoBasedOnOrgId(caseData, applicant2NoticeOfProceedingTemplateId, coversheet,
                        applicationTemplateId,applicationDocumentName, applicant);
            } else {
                log.info("Generating notice of proceedings for respondent for sole case id {} ", caseId);

                documentPackInfo = getOverseasBasedTemplateId(caseData, caseId, applicant2NoticeOfProceedingTemplateId, coversheet,
                        applicationTemplateId,applicationDocumentName, applicant);

                if (isCourtService
                        && !YES.equals(caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay())) {
                    log.info("Sending respondent AoS pack to bulk print.  Case ID: {}", caseId);
                    documentPackInfo = getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                            applicationTemplateId,applicationDocumentName);
                }
            }

            if (isSoleApplication) {
                caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());
            }
        }
        return documentPackInfo;
    }

    private DocumentPackInfo getDocumentPackInfoBasedOnOrgId(CaseData caseData,String applicant2NoticeOfProceedingTemplateId,
                                                             String coversheet, String applicationTemplateId,
                                                             String applicationDocumentName, Applicant applicant) {
        var hasSolicitor = applicant.getSolicitor() != null;
        var hasOrgId = hasSolicitor && applicant.getSolicitor().hasOrgId();

        if (hasOrgId) {
            if (!caseData.getApplication().isCourtServiceMethod()) {
                return getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                        applicationTemplateId,applicationDocumentName);
            } else {
                return getDocumentPackInfo(applicant2NoticeOfProceedingTemplateId,
                        applicationTemplateId,applicationDocumentName);
            }
        } else {
            return getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                    applicationTemplateId,applicationDocumentName);
        }
    }
    private DocumentPackInfo getOverseasBasedTemplateId(CaseData caseData, Long caseId, String applicant2NoticeOfProceedingTemplateId,
                                                        String coversheet, String applicationTemplateId,
                                                        String applicationDocumentName, Applicant applicant) {
        boolean reissuedAsOfflineAOS = OFFLINE_AOS.equals(caseData.getApplication().getReissueOption());

        if (applicant.isBasedOverseas()) {
            log.info("Generating NOP for overseas respondent for sole case id {} ", caseId);
            return  getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                    applicationTemplateId,applicationDocumentName);
        } else if (isEmpty(applicant.getEmail()) || applicant.isApplicantOffline() || reissuedAsOfflineAOS) {
            return getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                    applicationTemplateId,applicationDocumentName);
        } else {
            if (!caseData.getApplication().isCourtServiceMethod()) {
                return getDocumentPackInfoWithCoverSheet(applicant2NoticeOfProceedingTemplateId, coversheet,
                        applicationTemplateId,applicationDocumentName);
            }
            return getDocumentPackInfo(applicant2NoticeOfProceedingTemplateId,
                    applicationTemplateId,applicationDocumentName);
        }
    }
    private DocumentPackInfo getDocumentPackInfoWithCoverSheet(String noticeOfProceedingTemplateId, String coversheet, String appTemplateId,
                                                               String applicationDocumentName) {
        return new DocumentPackInfo(
                ImmutableMap.of(
                        NOTICE_OF_PROCEEDINGS_APP_2, Optional.of(noticeOfProceedingTemplateId),
                        COVERSHEET, Optional.of(coversheet),
                        APPLICATION, Optional.of(appTemplateId)
                ),
                ImmutableMap.of(
                        noticeOfProceedingTemplateId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                        coversheet, COVERSHEET_DOCUMENT_NAME,
                        appTemplateId, applicationDocumentName
                ));
    }

    private DocumentPackInfo getDocumentPackInfo(String noticeOfProceedingTemplateId, String appTemplateId,
                                                               String applicationDocumentName) {
        return new DocumentPackInfo(
                ImmutableMap.of(
                        NOTICE_OF_PROCEEDINGS_APP_2, Optional.of(noticeOfProceedingTemplateId),
                        APPLICATION, Optional.of(appTemplateId)
                ),
                ImmutableMap.of(
                        noticeOfProceedingTemplateId, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                        appTemplateId, applicationDocumentName
                ));
    }

    private String getCoverSheetTemplateId(Applicant applicant) {
        return applicant.isRepresented() ? COVERSHEET_APPLICANT2_SOLICITOR : COVERSHEET_APPLICANT;
    }

    private String getApplicationTemplateId(boolean isSoleApplication, boolean isJudicialSeparationCase) {
        if (isSoleApplication) {
            return isJudicialSeparationCase ? JUDICIAL_SEPARATION_SOLE_APPLICATION_TEMPLATE_ID
                    : DIVORCE_APPLICATION_SOLE;
        } else {
            return isJudicialSeparationCase ? JUDICIAL_SEPARATION_JOINT_APPLICATION_TEMPLATE_ID
                    : DIVORCE_APPLICATION_JOINT;
        }
    }

    private String getNoticeOfProceedingTemplateId(CaseData caseData, boolean isCourtService, boolean isJudicialSeparationCase, Long caseId, boolean isApplicant1) {

        var isSoleApplication = caseData.getApplicationType().isSole();

        if (isSoleApplication) {
            if (isJudicialSeparationCase) {
                return isApplicant1 ?
                        generateApplicant1SoleNoticeOfProceedingsForJudicialSeparationTemplateId(caseData, isCourtService, caseId) :
                        generateApplicant2SoleJSNoticeOfProceedings(caseData, caseId);
            }

            return isApplicant1 ? generateApplicant1SoleNoticeOfProceedingsTemplateId(caseData, isCourtService, caseId) :
                    generateApplicant2SoleNoticeOfProceedings(caseData, caseId);
        } else {
            if (isJudicialSeparationCase) {
                return isApplicant1 ? generateApplicant1JointJSNoticeOfProceedingsTemplateId(caseData,  caseId) :
                        generateApplicant2JointJSNoticeOfProceedings(caseData, caseId);
            }

            return isApplicant1 ? generateApplicant1JointNoticeOfProceedingsTemplate(caseData, caseId) :
                    generateApplicant2JointNoticeOfProceedings(caseData, caseId);
        }
    }

    private String generateApplicant1SoleNoticeOfProceedingsTemplateId(CaseData caseData, boolean isCourtService, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating notice of proceedings for applicant solicitor for case id {} ", caseId);

            return isCourtService ? NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS : NFD_NOP_AS2_SOLE_APP1_SOL_SS;
        } else {
            log.info("Generating notice of proceedings for applicant for sole case id {} ", caseId);

            return isCourtService ? NFD_NOP_A1_SOLE_APP1_CIT_CS : NFD_NOP_AL2_SOLE_APP1_CIT_PS;
        }
    }

    private String generateApplicant1SoleNoticeOfProceedingsForJudicialSeparationTemplateId(CaseData caseData, boolean isCourtService, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating notice of judicial separation proceedings for applicant solicitor for case id {} ", caseId);

            return isCourtService ? NFD_NOP_APP1_SOLICITOR_JS_SOLE : NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
        } else {
            log.info("Generating notice of judicial separation proceedings for applicant for case id {} ", caseId);

            return isCourtService ? NFD_NOP_APP1_JS_SOLE : NFD_NOP_APP1_JS_SOLE_OS_PS;
        }
    }

    private String generateApplicant1JointNoticeOfProceedingsTemplate(CaseData caseData, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating solicitor applicant 1 notice of proceedings for joint case id {} ", caseId);

           return NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 1 notice of proceedings for joint case id {} ", caseId);

            return NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }
    }

    private String generateApplicant1JointJSNoticeOfProceedingsTemplateId(CaseData caseData, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("setting applicant 1 solicitor notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            return NFD_NOP_APP1APP2_SOL_JS_JOINT;

        } else {
            log.info("Setting applicant 1 notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            return NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
        }
    }

    private String generateApplicant2SoleJSNoticeOfProceedings(CaseData caseData, Long caseId) {
        final Applicant applicant2 = caseData.getApplicant2();
        log.info("Generating NOP for JS respondent for sole case id {} ", caseId);

        return applicant2.isRepresented() ? NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID : NFD_NOP_APP2_JS_SOLE;
    }

    private String generateApplicant2SoleNoticeOfProceedings(final CaseData caseData, final Long caseId) {

        final Applicant applicant2 = caseData.getApplicant2();
        if (applicant2.isRepresented()) {
            log.info("Generating notice of proceedings for respondent solicitor for case id {} ", caseId);

            var hasSolicitor = applicant2.getSolicitor() != null;
            var hasOrgId = hasSolicitor && applicant2.getSolicitor().hasOrgId();

            if (hasOrgId) {
                return NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
            }

            return NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE;
        } else {
            log.info("Generating notice of proceedings for respondent for sole case id {} ", caseId);

            boolean reissuedAsOfflineAOS = OFFLINE_AOS.equals(caseData.getApplication().getReissueOption());

            if (applicant2.isBasedOverseas()) {
                log.info("Generating NOP for overseas respondent for sole case id {} ", caseId);
                return NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES;
            } else if (isEmpty(applicant2.getEmail()) || applicant2.isApplicantOffline() || reissuedAsOfflineAOS) {
                return reissuedAsOfflineAOS ? NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE : NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
            } else {
                return NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
            }
        }
    }

    private String generateApplicant2JointNoticeOfProceedings(final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating notice of proceedings for applicant 2 solicitor for case id {} ", caseId);

            return NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 2 notice of proceedings for applicant for joint case id {} ", caseId);

           return NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }
    }

    private String generateApplicant2JointJSNoticeOfProceedings(CaseData caseData, Long caseId) {

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating applicant 2 solicitor notice of proceedings for joint Judicial Separation case id {} ", caseId);

          return NFD_NOP_APP1APP2_SOL_JS_JOINT;
        } else {
            log.info("Generating applicant 2 notice of proceedings for joint Judicial Separation case id {} ", caseId);

            return NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
        }
    }
}
