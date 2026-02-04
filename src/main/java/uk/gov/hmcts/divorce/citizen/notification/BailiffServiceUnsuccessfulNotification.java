package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_UNSUCCESSFUL;

@Component
@Slf4j
@RequiredArgsConstructor
public class BailiffServiceUnsuccessfulNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant that Bailiff service was unsuccessful");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            BAILIFF_SERVICE_UNSUCCESSFUL,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }
}
