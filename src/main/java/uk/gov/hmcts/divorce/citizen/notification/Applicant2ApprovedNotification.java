package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class Applicant2ApprovedNotification implements ApplicantNotification {

    public static final String PAYS_FEES = "paysFees";
    public static final String IS_APPLICANT2_REPRESENTED = "isApplicant2Represented";
    public static final String IS_APPLICANT2_CITIZEN = "isApplicant2Citizen";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        Map<String, String> templateVars
            = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        EmailTemplateName templateId;

        if (caseData.getApplication().isHelpWithFeesApplication()
            && caseData.getApplication().getApplicant2HelpWithFees().getNeedHelp() != YesOrNo.YES) {
            log.info("Sending applicant 2 denied HWF notification to applicant 1 for case : {}", id);
            templateId = JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF;
        } else {
            log.info("Sending applicant 2 approved notification to applicant 1 for case : {}", id);
            templateId = JOINT_APPLICANT1_APPLICANT2_APPROVED;
            templateVars.put(PAYS_FEES, noFeesHelp(caseData) ? YES : NO);
            templateVars.put(IS_REMINDER, NO);
        }

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            templateId,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (caseData.getApplicant1().isRepresented()) {
            log.info("Sending applicant 2 approved (other applicant is represented) notification to applicant 2 for case : {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_APPLICANT2_APPROVED_SOLICITOR,
                applicant2TemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        } else {
            log.info("Sending applicant 2 approved notification to applicant 2 for case : {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICANT2_APPLICANT2_APPROVED,
                applicant2TemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending applicant 2 approved notification to applicant 1 solicitor for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR,
                applicant1SolicitorTemplateVars(caseData, id),
                caseData.getApplicant1().getLanguagePreference(),
                id
            );
        }
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(PAYS_FEES, noFeesHelp(caseData) ? YES : NO);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate()
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> applicant1SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));
        templateVars.put(IS_APPLICANT2_REPRESENTED, caseData.getApplicant2().isRepresented() ? YES : NO);
        templateVars.put(IS_APPLICANT2_CITIZEN, !caseData.getApplicant2().isRepresented() ? YES : NO);

        return templateVars;
    }

    private boolean noFeesHelp(CaseData caseData) {
        return caseData.getApplication().getApplicant1HelpWithFees().getNeedHelp() == YesOrNo.NO
            || caseData.getApplication().getApplicant2HelpWithFees().getNeedHelp() == YesOrNo.NO;
    }
}
