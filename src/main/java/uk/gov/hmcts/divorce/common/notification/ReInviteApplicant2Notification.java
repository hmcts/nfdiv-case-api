package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REINVITE_CITIZEN_TO_CASE;

@Component
@Slf4j
public class ReInviteApplicant2Notification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 2 to invite to case: {}", id);

        final Applicant applicant2 = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant2.getEmail(),
            REINVITE_CITIZEN_TO_CASE,
            commonContent.mainTemplateVars(caseData, id, applicant2, caseData.getApplicant1()),
            applicant2.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long id) {

    }
}
