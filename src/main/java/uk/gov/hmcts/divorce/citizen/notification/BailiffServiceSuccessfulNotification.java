package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_SUCCESSFUL;

@Component
@Slf4j
public class BailiffServiceSuccessfulNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant that Bailiff service was successful");

        final var templateContent = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        if (LanguagePreference.WELSH.equals(caseData.getApplicant1().getLanguagePreference())) {
            templateContent.put(CommonContent.PARTNER, commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2()));
        }

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            BAILIFF_SERVICE_SUCCESSFUL,
            templateContent,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
