package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant2AppliedForFinalOrderNotification implements ApplicantNotification {

    public static final String WILL_BE_CHECKED_WITHIN_2_DAYS = "will be checked within 2 days";
    public static final String WILL_BE_CHECKED_WITHIN_14_DAYS = "will be checked within 14 days";
    public static final String NOW_PLUS_14_DAYS = "now plus 14 days";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info("Sending Respondent notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                SOLE_APPLIED_FOR_FINAL_ORDER,
                applicant2TemplateVars(caseData, caseId),
                caseData.getApplicant2().getLanguagePreference()
            );
        } else {
            log.info("Sending Applicant 2 notification informing them that they have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER,
                jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference()
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
                jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && YES.equals(caseData.getFinalOrder().getApplicant2AppliedForFinalOrderFirst())) {
            log.info("Sending Applicant 1 notification informing them that other party have applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, CommonContent.NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, CommonContent.YES);
        templateVars.put(NOW_PLUS_14_DAYS, getNowPlus14Days());

        return templateVars;
    }

    private Map<String, String> jointApplicantTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, applicant, partner);

        templateVars.put(NOW_PLUS_14_DAYS, getNowPlus14Days());

        return templateVars;
    }

    private String getNowPlus14Days() {
        return LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER);
    }
}
