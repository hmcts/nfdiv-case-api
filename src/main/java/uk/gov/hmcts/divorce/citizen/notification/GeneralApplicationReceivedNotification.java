package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_RECEIVED;

@Component
@Slf4j
public class GeneralApplicationReceivedNotification implements ApplicantNotification {

    public static final String IS_DEEMED_SERVICE = "isDeemedService";
    public static final String IS_DISPENSE_SERVICE = "isDispenseService";
    public static final String IS_BAILIFF_SERVICE = "isBailiffService";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending general application received notification to applicant 1 for case : {}", id);

        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        setApplicationReceivedVars(caseData, templateVars);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            GENERAL_APPLICATION_RECEIVED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    private void setApplicationReceivedVars(CaseData caseData, Map<String, String> templateVars) {

        templateVars.put(IS_DEEMED_SERVICE, NO);
        templateVars.put(IS_DISPENSE_SERVICE, NO);
        templateVars.put(IS_BAILIFF_SERVICE, NO);

        AlternativeServiceType alternativeServiceType = caseData.getAlternativeService().getAlternativeServiceType();

        if (alternativeServiceType.equals(DEEMED)) {
            templateVars.put(IS_DEEMED_SERVICE, YES);
        } else if (alternativeServiceType.equals(DISPENSED)) {
            templateVars.put(IS_DISPENSE_SERVICE, YES);
        } else if (alternativeServiceType.equals(BAILIFF)) {
            templateVars.put(IS_BAILIFF_SERVICE, YES);
        }
    }
}
