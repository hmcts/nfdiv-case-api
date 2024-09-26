package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.divorce.common.notification.Applicant2RemindAwaitingJointFinalOrderNotification.DELAY_REASON_IF_OVERDUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_OR_FO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONSE_DUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant2AppliedForFinalOrderNotification implements ApplicantNotification {

    private static final String APP_2_OVERDUE_CONTENT = "They applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\n%s";

    private static final String DELAY_REASON = "delayReason";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info("Sending Respondent notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER,
                applicant2TemplateVars(caseData, caseId),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        } else {
            log.info("Sending Applicant 2 notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER,
                finalOrderNotificationCommonContent
                    .jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1(), false),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending Applicant 1 notification informing them that other party has applied for final order: {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                getTemplateVars(caseData, caseId),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    private Map<String, String> getTemplateVars(final CaseData caseData, Long caseId) {
        Map<String, String> templateVars = finalOrderNotificationCommonContent
            .jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), false);

        templateVars.put(DELAY_REASON_IF_OVERDUE,
            FinalOrderNotificationCommonContent.getPartnerDelayReason(
                caseData.getFinalOrder().getIsFinalOrderOverdue(),
                caseData.getFinalOrder().getApplicant2FinalOrderLateExplanation()));

        return templateVars;
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && YesOrNo.YES.equals(caseData.getFinalOrder().getApplicant2AppliedForFinalOrderFirst())) {
            log.info("Sending Applicant 1 solicitor notification informing them that other party have applied for final order: {}", caseId);

            Map<String, String> app1SolTemplateVars = commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());
            app1SolTemplateVars.put(DELAY_REASON, getApp2DelayReason(caseData));

            notificationService.sendEmail(
                    caseData.getApplicant1().getSolicitor().getEmail(),
                    JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                    app1SolTemplateVars,
                    caseData.getApplicant1().getLanguagePreference(),
                    caseId
            );
        }
    }

    private String getApp2DelayReason(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())
            ? Optional.ofNullable(caseData.getFinalOrder().getApplicant2FinalOrderLateExplanation())
            .map(APP_2_OVERDUE_CONTENT::formatted)
            .orElse(APP_2_OVERDUE_CONTENT.formatted(StringUtils.EMPTY))
            : StringUtils.EMPTY;
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 solicitor that their final order application has been submitted: {}", caseId);

            Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant2());
            templateVars.put(RESPONSE_DUE_DATE,
                caseData.getFinalOrder().getDateFinalOrderSubmitted().plusDays(14).format(DATE_TIME_FORMATTER));
            templateVars.put(CO_OR_FO, "final");

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, YES);
        templateVars.put(NOW_PLUS_14_DAYS, finalOrderNotificationCommonContent.getNowPlus14Days(caseData.getApplicant2()));
        // Whenever the applicant2 applies the template should print the isOverdue statement, if we added another variable for
        // if applicant 2 has applied, it would print 'must be reviewed by a judge' when applicant2 applies twice and is overdue
        // https://tools.hmcts.net/jira/browse/NFDIV-3687
        templateVars.put(IS_OVERDUE, YES);
        templateVars.put(IN_TIME, NO);
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }
}
