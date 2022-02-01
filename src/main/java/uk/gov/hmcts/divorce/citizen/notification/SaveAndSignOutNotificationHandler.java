package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;

@Component
public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void notifyApplicant(CaseData caseData, boolean isApplicant1) {
        final var self = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        final var partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        notificationService.sendEmail(
            isApplicant1 ? caseData.getApplicant1().getEmail() : caseData.getApplicant2EmailAddress(),
            SAVE_SIGN_OUT,
            commonContent.mainTemplateVars(caseData, null, self, partner),
            self.getLanguagePreference()
        );
    }
}
