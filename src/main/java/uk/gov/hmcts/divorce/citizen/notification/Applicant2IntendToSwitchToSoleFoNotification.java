package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTEND_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO;

@Component
@Slf4j
public class Applicant2IntendToSwitchToSoleFoNotification extends IntendToSwitchToSoleFoNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that applicant 2 intends to switch to sole fo : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(CaseData data, Long id) {
        log.info("Notifying applicant 2 that the court has received their intention to switch to sole fo : {}", id);

        notificationService.sendEmail(
            data.getApplicant2EmailAddress(),
            INTEND_TO_SWITCH_TO_SOLE_FO,
            templateVars(data, id, data.getApplicant2(), data.getApplicant1()),
            data.getApplicant2().getLanguagePreference(),
            id
        );
    }
}
