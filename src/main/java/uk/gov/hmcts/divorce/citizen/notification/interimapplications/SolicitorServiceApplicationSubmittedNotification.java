package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.MADE_PAYMENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.USED_HELP_WITH_FEES;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SERVICE_APPLICATION_SUBMITTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorServiceApplicationSubmittedNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        log.info("Sending service application submitted notification to applicant1 Solicitor1 on case id {}", caseId);

        final Map<String, String> templateVars =
            commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());

        addServiceApplicationTypeVars(templateVars, caseData);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLICITOR_SERVICE_APPLICATION_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private void addServiceApplicationTypeVars(Map<String, String> templateVars, CaseData caseData) {

        AlternativeService alternativeService = caseData.getAlternativeService();
        AlternativeServiceType serviceType = alternativeService.getAlternativeServiceType();

        commonContent.addServiceApplicationTypeVars(templateVars, serviceType);

        boolean usedHWF = ServicePaymentMethod.FEE_PAY_BY_HWF.equals(alternativeService.getServicePaymentFee().getPaymentMethod());
        templateVars.put(MADE_PAYMENT, !usedHWF ? YES : NO);
        templateVars.put(USED_HELP_WITH_FEES, usedHWF ? YES : NO);
    }
}
