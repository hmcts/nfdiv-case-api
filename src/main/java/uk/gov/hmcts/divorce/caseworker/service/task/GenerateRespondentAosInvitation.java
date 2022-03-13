package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CitizenRespondentAosInvitationTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetTemplateContent;
import uk.gov.hmcts.divorce.document.content.RespondentSolicitorAosInvitationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CITIZEN_RESP_AOS_INVITATION_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CITIZEN_RESP_AOS_INVITATION_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;

@Component
@Slf4j
public class GenerateRespondentAosInvitation implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    //TODO: Use correct template content when application template requirements are known.
    @Autowired
    private RespondentSolicitorAosInvitationTemplateContent respondentSolicitorAosInvitationTemplateContent;

    @Autowired
    private CitizenRespondentAosInvitationTemplateContent citizenRespondentAosInvitationTemplateContent;

    @Autowired
    private CoversheetTemplateContent coversheetTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating access code to allow the respondent to access the application");
        caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());

        if (caseData.getApplicationType().isSole()) {
            if (caseData.getApplicant2().isRepresented()) {
                log.info("Generating solicitor respondent AoS invitation for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    RESP_SOLICITOR_AOS_INVITATION,
                    respondentSolicitorAosInvitationTemplateContent.apply(caseData, caseId, caseDetails.getCreatedDate().toLocalDate()),
                    RESPONDENT_INVITATION,
                    RESP_AOS_INVITATION_DOCUMENT_NAME
                );
            } else if (isNotEmpty(caseData.getApplicant2().getEmail())) {
                log.info("Generating citizen respondent(with email) AoS invitation for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    CITIZEN_RESP_AOS_INVITATION_ONLINE,
                    citizenRespondentAosInvitationTemplateContent.apply(caseData, caseId),
                    RESPONDENT_INVITATION,
                    RESP_AOS_INVITATION_DOCUMENT_NAME
                );

                log.info("Generating coversheet for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    COVERSHEET_APPLICANT2,
                    coversheetTemplateContent.apply(caseData, caseId),
                    COVERSHEET,
                    COVERSHEET_DOCUMENT_NAME
                );
            } else if (isEmpty(caseData.getApplicant2().getEmail())) {
                log.info("Generating citizen respondent(without email) AoS invitation for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    CITIZEN_RESP_AOS_INVITATION_OFFLINE,
                    citizenRespondentAosInvitationTemplateContent.apply(caseData, caseId),
                    RESPONDENT_INVITATION,
                    RESP_AOS_INVITATION_DOCUMENT_NAME
                );

                log.info("Generating coversheet for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    COVERSHEET_APPLICANT2,
                    coversheetTemplateContent.apply(caseData, caseId),
                    COVERSHEET,
                    COVERSHEET_DOCUMENT_NAME
                );
            } else {
                log.info("Not generating AOS respondent invitation letter for case id {} ", caseId);
            }
        }

        return caseDetails;
    }

    private void generateDocumentAndUpdateCaseData(
        CaseDetails<CaseData, State> caseDetails,
        String templateId,
        Map<String, Object> templateContent,
        DocumentType documentType,
        String documentName
    ) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            documentType,
            templateContent,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, documentName, now(clock))
        );
    }
}
