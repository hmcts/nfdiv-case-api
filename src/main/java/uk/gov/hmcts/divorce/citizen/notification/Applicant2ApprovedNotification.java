package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
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
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
@Slf4j
public class Applicant2ApprovedNotification {

    public static final String NEED_FEES_HELP = "needHelpWithFees";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailTemplatesConfig configVars;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        log.info("Sending applicant 2 approved notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT2_APPROVED,
            applicant1TemplateVars(caseData, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant1WithDeniedHwf(CaseData caseData, Long id) {
        log.info("Sending applicant 2 denied HWF notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF,
            commonTemplateVars(caseData, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Sending applicant 2 approved notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_APPLICANT2_APPROVED,
            applicant2TemplateVars(caseData, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonTemplateVars(caseData, applicant, partner);
        templateVars.put(NEED_FEES_HELP, needsHwf(caseData) ? YES : NO);
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonTemplateVars(caseData, applicant, partner);
        templateVars.put(NEED_FEES_HELP, needsHwf(caseData) ? YES : NO);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private boolean needsHwf(CaseData caseData) {
        return caseData.getApplication().getApplicant1HelpWithFees().getNeedHelp() == YesOrNo.NO
            || caseData.getApplication().getApplicant2HelpWithFees().getNeedHelp() == YesOrNo.NO;
    }

    private Map<String, String> commonTemplateVars(CaseData caseData, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(IS_DIVORCE, isDivorce(caseData) ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !isDivorce(caseData) ? YES : NO);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(PARTNER, commonContent.getPartner(caseData, partner));
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            configVars.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        return templateVars;
    }
}
