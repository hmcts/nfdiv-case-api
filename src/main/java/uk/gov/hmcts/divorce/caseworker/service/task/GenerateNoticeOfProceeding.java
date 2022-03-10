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

        boolean isApplicant1Represented = caseData.getApplicant1().getSolicitorRepresented().toBoolean();
        boolean isApplicant2Represented = caseData.getApplicant2().getSolicitorRepresented().toBoolean();
        boolean isSoleApplication = caseData.getApplicationType().isSole();


        if (isSoleApplication && !isApplicant1Represented) {
            String templateId = caseData.getApplicant2().isBasedOverseas()
                ? NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID
                : NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;

            generateNoticeOfProceedings(
                caseData,
                caseId,
                templateId,
                isSoleApplication,
                isApplicant1Represented,
                isApplicant2Represented
            );

        } else if (!isSoleApplication && (!isApplicant1Represented || !isApplicant2Represented)) {
            generateNoticeOfProceedings(
                caseData,
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                isSoleApplication,
                isApplicant1Represented,
                isApplicant2Represented
            );
        } else {
            log.info("Not generating notice of proceedings for case id {} as did not match required criteria to generate document", caseId);
        }

        return caseDetails;
    }

    private void generateNoticeOfProceedings(CaseData caseData,
                                             Long caseId,
                                             String templateId,
                                             boolean isSoleApplication,
                                             boolean isApplicant1Represented,
                                             boolean isApplicant2Represented) {
        log.info("Generating notice of proceedings for case id {} ", caseId);

        if (isSoleApplication) {
            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                templateContent.apply(caseData, caseId),
                caseId,
                templateId,
                caseData.getApplicant1().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME
            );
        } else {
            // TODO: For the file name, do we want to differentiate as both currently will be called 'noticeOfProceedings.pdf'
            if (!isApplicant1Represented) {
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    NOTICE_OF_PROCEEDINGS,
                    jointTemplateContent.apply(caseData, caseId, caseData.getApplicant1()),
                    caseId,
                    templateId,
                    caseData.getApplicant1().getLanguagePreference(),
                    NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME);
            }

            if (!isApplicant2Represented) {
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    NOTICE_OF_PROCEEDINGS,
                    jointTemplateContent.apply(caseData, caseId, caseData.getApplicant2()),
                    caseId,
                    templateId,
                    caseData.getApplicant2().getLanguagePreference(),
                    NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME);
            }
        }
    }
}
