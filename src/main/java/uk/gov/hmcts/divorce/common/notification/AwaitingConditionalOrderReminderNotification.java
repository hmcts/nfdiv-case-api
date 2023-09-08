package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderReminderPrinter;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderReminderDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class AwaitingConditionalOrderReminderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GenerateD84Form generateD84Form;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Autowired
    private GenerateConditionalOrderReminderDocument generateConditionalOrderReminderDocument;

    @Autowired
    private ConditionalOrderReminderPrinter conditionalOrderReminderPrinter;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        if (!caseData.getSentNotifications().hasAwaitingConditionalOrderReminderNotificationSendToApplicant1()) {
            log.info("Sending reminder to applicant 1 that they can apply for a conditional order: {}", id);

            final Applicant applicant1 = caseData.getApplicant1();

            final Map<String, String> templateVars = commonContent
                .conditionalOrderTemplateVars(caseData, id, applicant1, caseData.getApplicant2());
            templateVars.put(IS_REMINDER, YES);

            notificationService.sendEmail(
                applicant1.getEmail(),
                CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                templateVars,
                applicant1.getLanguagePreference()
            );
            caseData.getSentNotifications()
                .setAwaitingConditionalOrderReminderNotificationSendToApplicant1(YesOrNo.YES);
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        if (!caseData.getSentNotifications().hasAwaitingConditionalOrderReminderNotificationSendToApplicant2()) {
            if (!caseData.getApplicationType().isSole() && nonNull(caseData.getApplicant2().getEmail())) {
                log.info("Sending reminder applicant 2 that they can apply for a conditional order: {}", id);

                final Applicant applicant2 = caseData.getApplicant2();

                final Map<String, String> templateVars = commonContent
                    .conditionalOrderTemplateVars(caseData, id, applicant2, caseData.getApplicant1());
                templateVars.put(IS_REMINDER, YES);

                notificationService.sendEmail(
                    applicant2.getEmail(),
                    CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                    templateVars,
                    applicant2.getLanguagePreference()
                );
            }
            caseData.getSentNotifications()
                .setAwaitingConditionalOrderReminderNotificationSendToApplicant2(YesOrNo.YES);
        }
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {

        if (!caseData.getSentNotifications().hasAwaitingConditionalOrderReminderNotificationSendToApplicant1Offline()) {
            log.info("Sending reminder applicant 1 offline that they can apply for a conditional order: {}", caseId);

            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT,
                coversheetApplicantTemplateContent.apply(caseData, caseId, caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference());

            generateConditionalOrderReminderDocument.generateConditionalOrderReminder(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2());

            generateD84Form.generateD84Document(
                caseData,
                caseId);

            conditionalOrderReminderPrinter.sendLetters(caseData, caseId, caseData.getApplicant1());

            caseData.getSentNotifications()
                .setAwaitingConditionalOrderReminderNotificationSendToApplicant1Offline(YesOrNo.YES);
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {

        if (!caseData.getSentNotifications().hasAwaitingConditionalOrderReminderNotificationSendToApplicant2Offline()
            && !caseData.getApplicationType().isSole()) {
            log.info("Sending reminder applicant 2 offline that they can apply for a conditional order for joint case: {}", caseId);

            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT,
                coversheetApplicantTemplateContent.apply(caseData, caseId, caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference());

            generateConditionalOrderReminderDocument.generateConditionalOrderReminder(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1());

            generateD84Form.generateD84Document(
                caseData,
                caseId);

            conditionalOrderReminderPrinter.sendLetters(caseData, caseId, caseData.getApplicant2());

            caseData.getSentNotifications()
                .setAwaitingConditionalOrderReminderNotificationSendToApplicant2Offline(YesOrNo.YES);
        }
    }
}
