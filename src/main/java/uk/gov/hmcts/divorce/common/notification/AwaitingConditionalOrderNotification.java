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
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class AwaitingConditionalOrderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that they can apply for a conditional order: {}", id);
        final Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, id, applicant1, caseData.getApplicant2());
        templateVars.put(IS_REMINDER, CommonContent.NO);

        notificationService.sendEmail(
            applicant1.getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars,
            applicant1.getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 solicitor that they can apply for a conditional order: {}", id);

        final Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);
        templateVars.put(SOLICITOR_NAME, applicant1.getSolicitor().getName());
        templateVars.put(UNION_TYPE, commonContent.getUnionType(caseData));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant1.getSolicitor().getReference())
            ? applicant1.getSolicitor().getReference()
            : "not provided");
        templateVars.put(SIGN_IN_URL, commonContent.getSignInUrl(caseData));

        notificationService.sendEmail(
            applicant1.getSolicitor().getEmail(),
            APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER,
            templateVars,
            applicant1.getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole() && nonNull(caseData.getApplicant2().getEmail())) {
            log.info("Notifying applicant 2 that they can apply for a conditional order: {}", id);
            final Applicant applicant2 = caseData.getApplicant2();
            final Map<String, String> templateVars = commonContent
                .conditionalOrderTemplateVars(caseData, id, applicant2, caseData.getApplicant1());
            templateVars.put(IS_REMINDER, CommonContent.NO);

            notificationService.sendEmail(
                applicant2.getEmail(),
                CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                templateVars,
                applicant2.getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 solicitor that they can apply for a conditional order: {}", id);

            final Applicant applicant2 = caseData.getApplicant2();
            final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);
            templateVars.put(SOLICITOR_NAME, applicant2.getSolicitor().getName());

            notificationService.sendEmail(
                applicant2.getSolicitor().getEmail(),
                SOLICITOR_AWAITING_CONDITIONAL_ORDER,
                templateVars,
                applicant2.getLanguagePreference()
            );
        }
    }
}
