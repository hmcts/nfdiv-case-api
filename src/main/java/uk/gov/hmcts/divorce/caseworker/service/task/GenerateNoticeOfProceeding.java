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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;


@Component
@Slf4j
public class GenerateNoticeOfProceeding implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private NoticeOfProceedingContent templateContent;

    @Autowired
    private NoticeOfProceedingJointContent jointTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        boolean isSoleApplication = caseData.getApplicationType().isSole();

        boolean isApplicant1Represented = caseData.getApplicant1().isRepresented();
        boolean isApplicant1Offline = caseData.getApplicant1().isOffline();

        boolean isApplicant2Represented = caseData.getApplicant2().isRepresented();
        boolean isApplicant2Offline = isBlank(caseData.getApplicant2().getEmail());

        if (isSoleApplication && !isApplicant1Represented) {

            String templateId = caseData.getApplicant2().isBasedOverseas()
                ? NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID
                : NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;

            log.info("Generating notice of proceedings for sole case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                templateContent.apply(caseData, caseId),
                caseId,
                templateId,
                caseData.getApplicant1().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME
            );

        }

        if (!isSoleApplication && (!isApplicant1Represented || isApplicant1Offline)) {

            log.info("Generating applicant 1 notice of proceedings for joint case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                jointTemplateContent.apply(caseData, caseId, caseData.getApplicant1()),
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME);

        }

        if (!isSoleApplication && (!isApplicant2Represented || isApplicant2Offline)) {

            log.info("Generating applicant 2 notice of proceedings for joint case id {} ", caseId);

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                jointTemplateContent.apply(caseData, caseId, caseData.getApplicant2()),
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                caseData.getApplicant2().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME);

        }

        return caseDetails;
    }
}
