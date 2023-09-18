package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_OR_FO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONSE_DUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.WILL_BE_CHECKED_WITHIN_2_DAYS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant1AppliedForFinalOrderNotification implements ApplicantNotification {

    private static final String APP_1_OVERDUE_CONTENT = "They applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\n%s";
    private static final String DELAY_REASON = "delayReason";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info("Sending Applicant notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                SOLE_APPLIED_FOR_FINAL_ORDER,
                applicant1TemplateVars(caseData, caseId),
                caseData.getApplicant1().getLanguagePreference()
            );
        } else {
            log.info("Sending Applicant 1 notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER,
                finalOrderNotificationCommonContent
                    .jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), false),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 1 solicitor that their final order application has been submitted: {}", caseId);

            Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());
            templateVars.put(RESPONSE_DUE_DATE,
                caseData.getFinalOrder().getDateFinalOrderSubmitted().plusDays(14).format(DATE_TIME_FORMATTER));
            templateVars.put(CO_OR_FO, "final");

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER,
                templateVars,
                ENGLISH
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending Applicant 2 notification informing them that other party has applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                finalOrderNotificationCommonContent
                    .jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1(), false),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
                && YesOrNo.YES.equals(caseData.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {
            log.info("Sending Applicant 2 solicitor notification informing them that other party have applied for final order: {}", caseId);

            Map<String, String> app2SolTemplateVars = commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant2());
            app2SolTemplateVars.put(DELAY_REASON, getApp1DelayReason(caseData));

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                app2SolTemplateVars,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    private String getApp1DelayReason(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())
            ? Optional.ofNullable(caseData.getFinalOrder().getApplicant1FinalOrderLateExplanation())
                .map(APP_1_OVERDUE_CONTENT::formatted)
                .orElse(APP_1_OVERDUE_CONTENT.formatted(StringUtils.EMPTY))
            : StringUtils.EMPTY;
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        boolean isFinalOrderEligible = caseData.getFinalOrder().getDateFinalOrderNoLongerEligible().isAfter(LocalDate.now(clock));

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, isFinalOrderEligible ? YES : NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, !isFinalOrderEligible ? YES : NO);
        templateVars.put(NOW_PLUS_14_DAYS, !isFinalOrderEligible ? finalOrderNotificationCommonContent
            .getNowPlus14Days(caseData.getApplicant1()) : "");

        return templateVars;
    }
}
