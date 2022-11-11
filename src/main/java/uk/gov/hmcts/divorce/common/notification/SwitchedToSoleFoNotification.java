package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;

@Component
@Slf4j
public class SwitchedToSoleFoNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;


    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        log.info("Notifying solicitor that they made a sole application for a final order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLIED_FOR_FINAL_ORDER,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        log.info("Notifying solicitor that they made a sole application for a final order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
