package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ConditionalOrderPronouncedNotification implements ApplicantNotification {

    private static final String CO_PRONOUNCEMENT_DATE_PLUS_43 = "CO pronouncement date plus 43 days";
    private static final String COURT_NAME = "court name";
    private static final String HEARING_DATE = "date of hearing";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Notifying respondent that their conditional order application has been granted: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(COURT_NAME, "what is the court name"); //TODO: court name
        templateVars.put(HEARING_DATE, caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
