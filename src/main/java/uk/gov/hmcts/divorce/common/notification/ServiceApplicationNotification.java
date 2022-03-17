package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DISPENSE_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED;

@Component
@Slf4j
public class ServiceApplicationNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    private static final String LOGGER_MESSAGE = "Notifying applicant that service application for {} was {}";

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            getEmailTemplate(caseData.getAlternativeService()),
            getServiceApplicationVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference());
    }

    private Map<String, String> getServiceApplicationVars(final CaseData caseData, final Long id) {

        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        AlternativeServiceType alternativeServiceType = caseData.getAlternativeService().getAlternativeServiceType();

        templateVars.put(IS_DEEMED_SERVICE, DEEMED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_DISPENSE_SERVICE, DISPENSED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_BAILIFF_SERVICE, BAILIFF.equals(alternativeServiceType) ? YES : NO);

        return templateVars;
    }

    private EmailTemplateName getEmailTemplate(final AlternativeService alternativeService) {
        boolean isServiceApplicationGranted = alternativeService.getServiceApplicationGranted().toBoolean();

        if (isServiceApplicationGranted) {
            log.info(LOGGER_MESSAGE, alternativeService.getAlternativeServiceType().getLabel(), "granted");
            return SERVICE_APPLICATION_GRANTED;
        } else {
            log.info(LOGGER_MESSAGE, alternativeService.getAlternativeServiceType().getLabel(), "rejected");
            return SERVICE_APPLICATION_REJECTED;
        }
    }
}
