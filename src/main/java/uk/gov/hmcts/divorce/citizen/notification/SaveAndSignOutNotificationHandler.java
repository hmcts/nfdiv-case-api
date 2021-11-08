package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;

@Component
public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void notifyApplicant(CaseData caseData, UserDetails user) {
        final var invite = caseData.getCaseInvite();
        final var isTriggeredByApp2 = invite != null && user.getId().equals(invite.getApplicant2UserId());
        final var self = isTriggeredByApp2 ? caseData.getApplicant2() : caseData.getApplicant1();
        final var partner = isTriggeredByApp2 ? caseData.getApplicant1() : caseData.getApplicant2();

        notificationService.sendEmail(
            user.getEmail(),
            SAVE_SIGN_OUT,
            commonContent.templateVars(caseData, null, self, partner),
            self.getLanguagePreference()
        );
    }
}
