package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.PLUS_21_DUE_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant2SwitchToSoleCoNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 of his partner's CO application as Sole for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            PARTNER_SWITCHED_TO_SOLE_CO,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Notifying applicant 1 of his CO application for case : {}", id);
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(PLUS_21_DUE_DATE,
            caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate().plusDays(21).format(DATE_TIME_FORMATTER));

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
