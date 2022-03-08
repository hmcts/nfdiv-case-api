package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

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

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        ApplicationType applicationType = caseData.getApplicationType();
        boolean isApplicant1Represented = caseData.getApplicant1().getSolicitorRepresented().toBoolean();
        boolean isApplicant2Represented = caseData.getApplicant2().getSolicitorRepresented().toBoolean();

        if (applicationType.isSole() && !isApplicant1Represented) {
            String templateId = caseData.getApplicant2().isBasedOverseas()
                ? NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID
                : NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;

            generateNoticeOfProceedings(
                caseData,
                caseId,
                templateId
            );

        } else if (!applicationType.isSole() && isApplicant1Represented && !isApplicant2Represented) {
            // Joint Application, App 1 is solicitor, App 2 is citizen
            generateNoticeOfProceedings(
                caseData,
                caseId,
                JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID
            );
        } else {
            log.info("Not generating notice of proceedings(sole) as application type needs to sole and should be citizen application");
        }

        return caseDetails;
    }

    private void generateNoticeOfProceedings(CaseData caseData,
                                             Long caseId,
                                             String templateId) {

        log.info("Generating notice of proceedings for case id {} ", caseId);

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
}
