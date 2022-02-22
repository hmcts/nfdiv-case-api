package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentSolicitorAosInvitationTemplateContent;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CITIZEN_RESP_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;

@Component
@Slf4j
public class GenerateRespondentAosInvitation implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    //TODO: Use correct template content when application template requirements are known.
    @Autowired
    private RespondentSolicitorAosInvitationTemplateContent templateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();

        log.info("Generating access code to allow the respondent to access the application");
        caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());

        final String templateId;

        if (caseData.getApplicant2().isRepresented()) {
            log.info("Generating solicitor respondent AoS invitation for case id {} ", caseId);
            templateId = RESP_SOLICITOR_AOS_INVITATION;
        } else {
            log.info("Generating citizen respondent AoS invitation for case id {} ", caseId);
            templateId = CITIZEN_RESP_AOS_INVITATION;
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            RESPONDENT_INVITATION,
            templateContent.apply(caseData, caseId, createdDate),
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, RESP_AOS_INVITATION_DOCUMENT_NAME, now(clock))
        );

        return caseDetails;
    }
}
