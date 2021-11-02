package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.THEIR_EMAIL_ADDRESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
@Slf4j
public class Applicant1ResubmitNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig configVars;

    public void sendToApplicant1(CaseData caseData, Long id) {
        log.info("Sending applicant 1 made changes notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE,
            applicant1TemplateVars(caseData),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Sending applicant 1 made changes notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE,
            applicant2TemplateVars(caseData),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData) {
        Map<String, String> templateVars = resubmitTemplateVars(caseData, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(THEIR_EMAIL_ADDRESS, caseData.getCaseInvite().getApplicant2InviteEmailAddress());
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData) {
        Map<String, String> templateVars = resubmitTemplateVars(caseData, caseData.getApplicant2(), caseData.getApplicant1());
        String signInLink = configVars.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL);
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY, signInLink + "/applicant2/check-your-joint-application");
        return templateVars;
    }

    private Map<String, String> resubmitTemplateVars(CaseData caseData, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(IS_DIVORCE, isDivorce(caseData) ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !isDivorce(caseData) ? YES : NO);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(PARTNER, commonContent.getPartner(caseData, partner));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            configVars.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        templateVars.put(COURT_EMAIL,
            configVars.getTemplateVars().get(isDivorce(caseData) ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        return templateVars;
    }
}
