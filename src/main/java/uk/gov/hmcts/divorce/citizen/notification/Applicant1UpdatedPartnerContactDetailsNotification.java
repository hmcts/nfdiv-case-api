package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APP1_UPDATED_PARTNER_CONTACT_DETAILS;

@Component
@RequiredArgsConstructor
@Slf4j
public class Applicant1UpdatedPartnerContactDetailsNotification
    implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;


    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();

        log.info("Notifying applicant about partner's contact details update and serve documents for overseas: {}, with template: {}",
            caseId, APP1_UPDATED_PARTNER_CONTACT_DETAILS);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APP1_UPDATED_PARTNER_CONTACT_DETAILS,
            commonContent.mainTemplateVars(caseData, caseId, applicant1, applicant2),
            applicant1.getLanguagePreference(),
            caseId
        );
    }
}
