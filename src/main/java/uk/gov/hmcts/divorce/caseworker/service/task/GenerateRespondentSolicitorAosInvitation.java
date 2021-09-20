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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator.generateAccessCode;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;

@Component
@Slf4j
public class GenerateRespondentSolicitorAosInvitation implements CaseTask {

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

        log.info("Executing handler for generating respondent aos invitation for case id {} ", caseId);

        if (caseDetails.getData().getApplication().isSolicitorApplication() && caseData.getApplicant2().isRepresented()) {

            final Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, caseId, createdDate);

            caseData.getCaseInvite().setAccessCode(generateAccessCode());

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                RESPONDENT_INVITATION,
                templateContentSupplier,
                caseId,
                RESP_SOLICITOR_AOS_INVITATION,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(caseId, RESP_AOS_INVITATION_DOCUMENT_NAME, LocalDateTime.now(clock))
            );
        }

        return caseDetails;
    }
}
