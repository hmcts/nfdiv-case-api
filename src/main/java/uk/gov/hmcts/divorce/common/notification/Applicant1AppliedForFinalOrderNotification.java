package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant1AppliedForFinalOrderNotification implements ApplicantNotification {

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
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        if (caseData.getApplicationType().isSole()) {
            log.info("Sending Applicant 1 notification informing them that they have applied for final order: {}", id);
            notificationService.sendEmail(
                    caseData.getApplicant1().getEmail(),
                    SOLE_APPLIED_FOR_FINAL_ORDER,
                    applicant1TemplateVars(caseData, id),
                    caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            if (Objects.isNull(caseData.getFinalOrder().getApplicant2AppliedForFinalOrder())) {
                log.info("Sending Applicant 2 notification informing them that other party have applied for final order: {}", caseId);
                notificationService.sendEmail(
                        caseData.getApplicant2().getSolicitor().getEmail(),
                        JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                        commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant2()),
                        ENGLISH
                );
            } else {
                ApplicantNotification.sendBothSolicitorsAppliedForFinalOrderNotification(caseData, caseId, caseData.getApplicant2(),
                        commonContent, notificationService);
            }
        }
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole() && Objects.nonNull(caseData.getFinalOrder().getApplicant2AppliedForFinalOrder())) {
            log.info("Sending Applicants notification informing them that both parties have applied for final order: {}", caseId);
            ApplicantNotification.sendBothSolicitorsAppliedForFinalOrderNotification(caseData, caseId, caseData.getApplicant1(),
                    commonContent, notificationService);
        }
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        boolean isFinalOrderEligible = caseData.getFinalOrder().getDateFinalOrderNoLongerEligible().isAfter(LocalDate.now(clock));

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, isFinalOrderEligible ? YES : NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, !isFinalOrderEligible ? YES : NO);
        templateVars.put(NOW_PLUS_14_DAYS, !isFinalOrderEligible ? getNowPlus14Days() : "");

        return templateVars;
    }

    private String getNowPlus14Days() {
        return LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER);
    }
}
