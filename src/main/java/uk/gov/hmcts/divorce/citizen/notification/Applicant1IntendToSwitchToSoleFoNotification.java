package uk.gov.hmcts.divorce.citizen.notification;

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

import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTEND_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class Applicant1IntendToSwitchToSoleFoNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that the court has received their intention to switch to sole fo : {}", id);

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            INTEND_TO_SWITCH_TO_SOLE_FO,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData data, Long id) {
        log.info("Notifying applicant 2 that applicant 1 intends to switch to sole fo : {}", id);

        Map<String, String> templateVars = commonContent.mainTemplateVars(data, id, data.getApplicant2(), data.getApplicant1());
        templateVars.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        notificationService.sendEmail(
            data.getApplicant2EmailAddress(),
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
            templateVars,
            data.getApplicant2().getLanguagePreference()
        );
    }
}
