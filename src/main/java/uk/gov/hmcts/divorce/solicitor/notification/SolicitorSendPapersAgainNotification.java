package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SENT_PAPERS_AGAIN;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorSendPapersAgainNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (caseData.getApplicationType().isSole()) {

            Applicant applicant = caseData.getApplicant1();

            var templateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);

            log.info("Sending Applicant 1 Solicitor notification informing them that they sent papers again: {}", caseId);

            notificationService.sendEmail(
                applicant.getSolicitor().getEmail(),
                SOLICITOR_SENT_PAPERS_AGAIN,
                templateVars,
                applicant.getLanguagePreference(),
                caseId
            );
        }
    }
}
