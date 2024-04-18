package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.notification.CommonContent.FEES_FINANCIALORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINANCIAL_ORDER_NOT_REQUESTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINANCIAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINANCIAL_ORDER_REQUESTED_NOTIFICATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_FINANCIAL_ORDER_REQUESTED_NOTIFICATION;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_MISC;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_FINANCIALORDERNOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
public class FinancialOrderRequestedNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private PaymentService paymentService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant1().getEmail();
        final LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();

        log.info("Sending financial order requested notification to applicant 1 for case : {}", caseId);

        final Map<String, String> templateVars =
            populateTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());

        notificationService.sendEmail(
            email,
            FINANCIAL_ORDER_REQUESTED_NOTIFICATION,
            templateVars,
            languagePreference,
            caseId
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant2EmailAddress();
        final LanguagePreference languagePreference = caseData.getApplicant2().getLanguagePreference();

        final Map<String, String> templateVars =
            populateTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());

        if (caseData.getApplicationType().isSole()) {
            if (isNotBlank(email)) {
                log.info("Sending financial order requested notification to respondent for case : {}", caseId);

                notificationService.sendEmail(
                    email,
                    RESPONDENT_FINANCIAL_ORDER_REQUESTED_NOTIFICATION,
                    templateVars,
                    languagePreference,
                    caseId
                );
            } else {
                log.info("Respondent email is blank. Financial Order Notification will not be sent.");
            }
        } else {
            log.info("Sending financial order requested notification to applicant 2 for case : {}", caseId);

            notificationService.sendEmail(
                email,
                FINANCIAL_ORDER_REQUESTED_NOTIFICATION,
                templateVars,
                languagePreference,
                caseId
            );
        }
    }

    public Map<String, String> populateTemplateVars(final CaseData caseData,
                                                    final Long id,
                                                    final Applicant applicant,
                                                    final Applicant partner) {
        final Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, applicant, partner);

        if (YesOrNo.YES.equals(applicant.getFinancialOrder())) {
            templateVars.put(FINANCIAL_ORDER_REQUESTED,YES);
            templateVars.put(FINANCIAL_ORDER_NOT_REQUESTED,NO);
        } else {
            templateVars.put(FINANCIAL_ORDER_REQUESTED,NO);
            templateVars.put(FINANCIAL_ORDER_NOT_REQUESTED,YES);
        }

        String financialOrderCost = formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_MISC, KEYWORD_FINANCIALORDERNOTICE));
        templateVars.put(FEES_FINANCIALORDER, financialOrderCost);

        return templateVars;
    }
}
