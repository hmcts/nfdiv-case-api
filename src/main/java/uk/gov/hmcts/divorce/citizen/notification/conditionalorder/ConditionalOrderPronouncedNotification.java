package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ConditionalOrderPronouncedNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that their conditional order application has been pronounced: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_CONDITIONAL_ORDER_PRONOUNCED,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 2 that their conditional order application has been granted: {}", id);

        EmailTemplateName emailTemplateName =
            caseData.getApplicationType().isSole() ? SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED : CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            emailTemplateName,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
