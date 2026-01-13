package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED_BY_CASEWORKER;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceApplicationRejectedNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    private static final String SERVICE_APPLICATION_NAME = "service_application_name";

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Sending service application rejected notification to applicant 1 on case id {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SERVICE_APPLICATION_REJECTED_BY_CASEWORKER,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        AlternativeService serviceApplication = caseData.getAlternativeService();

        templateVars.put(SERVICE_APPLICATION_NAME, getServiceApplicationName(serviceApplication.getAlternativeServiceType(),
            applicant.getLanguagePreferenceWelsh().toBoolean()));

        return templateVars;
    }

    static String getServiceApplicationName(AlternativeServiceType type, boolean isWelsh) {
        switch (type) {
            case DEEMED:
                return isWelsh ? "cyflwyno tybiedig" : "deemed service";
            case DISPENSED:
                return isWelsh ? "hepgor cyflwyno" : "dispensed with service";
            case BAILIFF:
                return isWelsh ? "gwasanaeth beili" : "bailiff service";
            case ALTERNATIVE_SERVICE:
                return isWelsh ? "cyflwyno amgen" : "alternative service";
            default:
                return "";
        }
    }
}
