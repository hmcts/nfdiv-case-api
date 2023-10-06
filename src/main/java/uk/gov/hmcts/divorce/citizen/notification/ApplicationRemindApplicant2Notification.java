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

import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REMINDER_WHEN_APPLICANT1_REPRESENTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class ApplicationRemindApplicant2Notification implements ApplicantNotification {

    public static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    public static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending reminder to applicant 2 to review case : {}", id);

        if (caseData.getApplicant1().isRepresented()) {

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_REMINDER_WHEN_APPLICANT1_REPRESENTED,
                solicitorTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );

        } else {
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
                citizenTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );

        }


    }

    private Map<String, String> citizenTemplateVars(CaseData caseData,
                                                    Long id,
                                                    Applicant applicant,
                                                    Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(IS_REMINDER, YES);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate()
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
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
