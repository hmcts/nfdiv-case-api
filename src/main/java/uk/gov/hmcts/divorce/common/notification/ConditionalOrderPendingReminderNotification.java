package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class ConditionalOrderPendingReminderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (caseData.getConditionalOrder().isConditionalOrderPending()) {
            log.info("Notifying applicant 1 that they can apply for a conditional order: {}", id);

            final Applicant applicant1 = caseData.getApplicant1();

            final Map<String, String> templateVars = commonContent
                .conditionalOrderTemplateVars(caseData, id, applicant1, caseData.getApplicant2());
            templateVars.put(IS_REMINDER, YES);

            notificationService.sendEmail(
                applicant1.getEmail(),
                CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                templateVars,
                applicant1.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getConditionalOrder().isConditionalOrderPending()
            && nonNull(caseData.getApplicant2().getEmail())) {
            log.info("Notifying applicant 2 that they can apply for a conditional order: {}", id);
            final Applicant applicant2 = caseData.getApplicant2();

            final Map<String, String> templateVars = commonContent
                .conditionalOrderTemplateVars(caseData, id, applicant2, caseData.getApplicant1());
            templateVars.put(IS_REMINDER, YES);

            notificationService.sendEmail(
                applicant2.getEmail(),
                CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                templateVars,
                applicant2.getLanguagePreference(),
                id
            );
        }
    }
}
