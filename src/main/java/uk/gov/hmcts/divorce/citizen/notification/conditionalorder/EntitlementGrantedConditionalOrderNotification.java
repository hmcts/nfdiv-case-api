package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING_MINUS_SEVEN_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.ENTITLEMENT_GRANTED_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;

@Component
@Slf4j
public class EntitlementGrantedConditionalOrderNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending entitlement granted on conditional order notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            ENTITLEMENT_GRANTED_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference());
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending entitlement granted on conditional order notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            ENTITLEMENT_GRANTED_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference());
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();

        templateVars.put(COURT_NAME, conditionalOrder.getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING, dateAndTimeOfHearing.format(DATE_TIME_FORMATTER));
        templateVars.put(TIME_OF_HEARING, dateAndTimeOfHearing.format(TIME_FORMATTER));
        templateVars.put(DATE_OF_HEARING_MINUS_SEVEN_DAYS, dateAndTimeOfHearing.minus(7, DAYS).format(DATE_TIME_FORMATTER));

        return templateVars;
    }
}
