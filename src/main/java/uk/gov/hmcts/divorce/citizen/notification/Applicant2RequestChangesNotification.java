package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_COMMENTS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.THE_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class Applicant2RequestChangesNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICANT_2_COMMENTS, caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(FOR_THE_APPLICATION, THE_DIVORCE);
        } else {
            templateVars.put(FOR_THE_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending notification to applicant 1 to request changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateVars.put(LAST_NAME, caseData.getApplicant2().getLastName());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending notification to applicant 2 to confirm their request for changes: {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_REQUEST_CHANGES,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
