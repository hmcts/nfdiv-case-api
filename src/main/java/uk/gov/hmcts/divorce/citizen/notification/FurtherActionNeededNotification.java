package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.AWAITING_APPLICANT_NO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
@RequiredArgsConstructor
public class FurtherActionNeededNotification implements ApplicantNotification {

    private static final String ALTERNATIVE_APPLICATION_FEE = "alternativeApplicationFee";

    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        log.info("Sending Applicant 1 notification notifying them further action needed: {}", caseId);

        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(ALTERNATIVE_APPLICATION_FEE,
            formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL,KEYWORD_WITHOUT_NOTICE)));
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            AWAITING_APPLICANT_NO_RESPONDENT_ADDRESS,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }
}
