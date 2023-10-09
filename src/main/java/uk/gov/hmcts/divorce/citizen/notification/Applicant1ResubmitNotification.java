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

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class Applicant1ResubmitNotification implements ApplicantNotification {

    public static final String THEIR_EMAIL_ADDRESS = "their email address";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig configVars;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending applicant 1 made changes notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE,
            applicant1TemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (caseData.getApplicant1().isRepresented()) {
            log.info("Sending applicant 1's solicitor made changes notification to applicant 2 for case : {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR,
                applicant2TemplateVars(caseData, id),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        } else {
            log.info("Sending applicant 1 made changes notification to applicant 2 for case : {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE,
                applicant2TemplateVars(caseData, id),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {

        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying Applicant2Solicitor that Applicant 1 has made changes : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE,
                applicant2SolicitorTemplateVars(caseData, id),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = resubmitTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(THEIR_EMAIL_ADDRESS, caseData.getApplicant2EmailAddress());
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = resubmitTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        String signInLink = configVars.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL);
        templateVars.put(SIGN_IN_URL, signInLink + "applicant2/check-your-joint-application");
        return templateVars;
    }

    private Map<String, String> resubmitTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate()
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> applicant2SolicitorTemplateVars(CaseData caseData, Long id) {
        var templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        return templateVars;
    }
}
