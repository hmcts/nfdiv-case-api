package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ApplicationJointTemplateContent;
import uk.gov.hmcts.divorce.document.content.DivorceApplicationSoleTemplateContent;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_SOLE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class GenerateDivorceApplication implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private DivorceApplicationSoleTemplateContent divorceApplicationSoleTemplateContent;

    @Autowired
    private ApplicationJointTemplateContent applicationJointTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();

        log.info("Executing handler for generating mini application for case id {} ", caseId);

        if (caseData.getApplicationType().isSole()) {
            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                divorceApplicationSoleTemplateContent.apply(caseData, caseId, createdDate),
                caseId,
                DIVORCE_APPLICATION_SOLE,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, DIVORCE_APPLICATION_DOCUMENT_NAME, now(clock))
            );
        } else {
            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                applicationJointTemplateContent.apply(caseData, caseId, createdDate),
                caseId,
                DIVORCE_APPLICATION_JOINT,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, DIVORCE_APPLICATION_DOCUMENT_NAME, now(clock))
            );
        }

        return caseDetails;
    }
}
