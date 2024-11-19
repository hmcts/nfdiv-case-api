package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_APPLICANT2_REQUESTED_CHANGES;

@Component
@Slf4j
public class Applicant2RequestChangesNotification implements ApplicantNotification {

    public static final String APPLICANT_2_COMMENTS = "applicant 2 comments";
    public static final String PARTNER_IS_REPRESENTED = "partner is represented";
    public static final String REQUESTED_CHANGES = "requested changes explanation";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICANT_2_COMMENTS, caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation());

        log.info("Sending notification to applicant 1 to request changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 solicitor that applicant 2 has requested changes: {}", id);

        final Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, applicant1.getSolicitor().getName());
        templateVars.put(PARTNER_IS_REPRESENTED, caseData.getApplicant2().isRepresented() ? "Yes" : "No");
        templateVars.put(SIGN_IN_URL, commonContent.getSignInUrl(caseData));
        templateVars.put(REQUESTED_CHANGES,
            caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation());

        notificationService.sendEmail(
            applicant1.getSolicitor().getEmail(),
            SOLICITOR_APPLICANT2_REQUESTED_CHANGES,
            templateVars,
            applicant1.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (caseData.getApplicant1().isRepresented()) {
            log.info("Sending notification to applicant 2 to confirm their request for changes (applicant 1 is represented): {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                APPLICANT2_APPLICANT1_SOLICITOR_REPRESENTED_REQUESTED_CHANGES,
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        } else {
            log.info("Sending notification to applicant 2 to confirm their request for changes (applicant 1 is not represented): {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_REQUEST_CHANGES,
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }
}
