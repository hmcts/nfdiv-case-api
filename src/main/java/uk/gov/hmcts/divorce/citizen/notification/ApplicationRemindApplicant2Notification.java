package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.*;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_WHEN_APPLICANT1ISREPRESENTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ApplicationRemindApplicant2Notification implements ApplicantNotification {

    public static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    public static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";
    public static final String SOLICITOR_FIRM = "solicitor firm"; // make sure to add "solicitor firm" on the template where we want the solicitor firm address to be added

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending reminder to applicant 2 to review case : {}", id);

        if (caseData.getApplicant1().isRepresented()) { // here app1 is represented hence we will need to send reminder email with the new template

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_WHEN_APPLICANT1ISREPRESENTED,//solicitor email template
                solicitorTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference()
                // solicitor email template variables
            );

        } else { // applicant 1 is a citizen in this scenario and send email template to app2
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
                citizenTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(), true),
                caseData.getApplicant1().getLanguagePreference()
            );

        }


    }

    private Map<String, String> citizenTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner, boolean isReminder) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(IS_REMINDER, isReminder ? YES : NO);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        templateVars.put(CREATE_ACCOUNT_LINK,
            config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));
        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        templateVars.put(CREATE_ACCOUNT_LINK,
            config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));

        return templateVars;
    }
}
