package uk.gov.hmcts.divorce.common.notification;

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

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;

@Component
@Slf4j
public class SwitchedToSoleFoNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;


    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 1 that they made a sole application for a final order: {}", id);

        final Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        if (YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            templateVars.put(IS_OVERDUE, YES);
            templateVars.put(IN_TIME, NO);
        } else {
            templateVars.put(IS_OVERDUE, NO);
            templateVars.put(IN_TIME, YES);
        }
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLIED_FOR_FINAL_ORDER,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 2 that they made a sole application for a final order: {}", id);
        final Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        if (YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            templateVars.put(IS_OVERDUE, YES);
            templateVars.put(IN_TIME, NO);
        } else {
            templateVars.put(IS_OVERDUE, NO);
            templateVars.put(IN_TIME, YES);
        }

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        log.info("Notifying solicitor that other party has applied for a final order as a sole: {}", caseId);

        final Applicant applicant = caseData.getApplicant2();

        Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER,
            templateVars,
            applicant.getLanguagePreference()
        );
    }
}
