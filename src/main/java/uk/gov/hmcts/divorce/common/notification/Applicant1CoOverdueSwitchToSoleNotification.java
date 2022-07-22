package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SWITCH_TO_SOLE_CO_OVERDUE;

@Component
@Slf4j
public class Applicant1CoOverdueSwitchToSoleNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Notifying applicant1 solicitor they can apply for switch to sole (Conditional order overdue) for application {} is overdue", id);

        // TODO: (Aaron) - Once template has been split on gov notify, set correct template vars.
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            APPLICANT_SOLICITOR_SWITCH_TO_SOLE_CO_OVERDUE,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
