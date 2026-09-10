package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_DETAILS_UPDATED;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondentDetailsUpdatedNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        var templateVars = commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());

        log.info("Sending Applicant 1 Solicitor notification informing them that respondent details are updated: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            RESPONDENT_DETAILS_UPDATED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }
}
