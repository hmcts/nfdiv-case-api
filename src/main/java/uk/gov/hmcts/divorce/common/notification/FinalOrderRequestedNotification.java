package uk.gov.hmcts.divorce.common.notification;

import com.microsoft.applicationinsights.web.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class FinalOrderRequestedNotification implements ApplicantNotification {
    public static final String APPLICANT_1_OVERDUE_CONTENT = "applicant1OverdueContent";
    public static final String APPLICANT_2_OVERDUE_CONTENT = "applicant2OverdueContent";

    public static final String DELAY_REASON = "%s applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\n%s";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying Applicant 1 solicitor that both applicants have applied for final order for case {}", caseId);
            var templateVars = solicitorsFinalOrderTemplateVars(caseData, caseId, caseData.getApplicant1());
            notificationService.sendEmail(
                    caseData.getApplicant1().getSolicitor().getEmail(),
                    JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
                    templateVars,
                    caseData.getApplicant1().getLanguagePreference(),
                    caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying Applicant 2 solicitor that both applicants have applied for final order for case {}", caseId);
            var templateVars = solicitorsFinalOrderTemplateVars(caseData, caseId, caseData.getApplicant2());
            notificationService.sendEmail(
                    caseData.getApplicant2().getSolicitor().getEmail(),
                    JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
                    templateVars,
                    caseData.getApplicant2().getLanguagePreference(),
                    caseId
            );
        }
    }

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
                && YesOrNo.YES.equals(caseData.getFinalOrder().getApplicant2AppliedForFinalOrderFirst())) {
            log.info("Notifying Applicant 1 that both applicants have applied for final order for case {}", caseId);
            var templateVars = applicantFinalOrderTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER,
                templateVars,
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
                && YesOrNo.YES.equals(caseData.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {
            log.info("Notifying Applicant 2 that both applicants have applied for final order for case {}", caseId);
            var templateVars = applicantFinalOrderTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private Map<String, String> solicitorsFinalOrderTemplateVars(final CaseData caseData, final Long caseId, Applicant applicant) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(IS_CONDITIONAL_ORDER, NO);
        templateVars.put(IS_FINAL_ORDER, YES);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
                isNotEmpty(applicant.getSolicitor().getReference()) ? applicant.getSolicitor().getReference() : NOT_PROVIDED);
        templateVars.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        applicantFinalOrderOverdueTemplateVars(templateVars, caseData);

        return templateVars;
    }

    private Map<String, String> applicantFinalOrderTemplateVars(final CaseData caseData, final Long caseId,
                                                                Applicant applicant1, Applicant applicant2) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant1, applicant2);
        applicantFinalOrderOverdueTemplateVars(templateVars, caseData);
        if (YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            templateVars.put(IS_OVERDUE, YES);
            templateVars.put(IN_TIME, NO);
        } else {
            templateVars.put(IS_OVERDUE, NO);
            templateVars.put(IN_TIME, YES);
        }

        return templateVars;
    }

    private void applicantFinalOrderOverdueTemplateVars(Map<String, String> templateVars, final CaseData caseData) {
        String applicant1OverdueContent = Optional.ofNullable(caseData.getFinalOrder().getApplicant1FinalOrderLateExplanation())
            .map(reason -> DELAY_REASON.formatted(caseData.getApplicant1().getFullName(), reason))
            .orElse(StringUtils.EMPTY);

        String applicant2OverdueContent = Optional.ofNullable(caseData.getFinalOrder().getApplicant2FinalOrderLateExplanation())
            .map(reason -> DELAY_REASON.formatted(caseData.getApplicant2().getFullName(), reason))
            .orElse(StringUtils.EMPTY);

        templateVars.put(APPLICANT_1_OVERDUE_CONTENT, applicant1OverdueContent);
        templateVars.put(APPLICANT_2_OVERDUE_CONTENT, applicant2OverdueContent);
    }
}
