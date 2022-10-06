package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_SUBMISSION_DATE_PLUS_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class Applicant2SwitchToSoleCoNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 of partner's CO application as Sole for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            PARTNER_SWITCHED_TO_SOLE_CO,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 1 solicitor that the other party made a sole application for a conditional order: {}", id);

        final Applicant applicant = caseData.getApplicant1();

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, id, applicant);
        templateVars.put(APPLICANT_NAME, applicant.getFullName());

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER,
            templateVars,
            applicant.getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData data, Long id) {
        log.info("Notifying applicant 1 of CO application for case : {}", id);

        Map<String, String> templateVars = commonContent.mainTemplateVars(data, id, data.getApplicant2(), data.getApplicant1());
        templateVars.put(PRONOUNCE_BY_DATE,
            data.getConditionalOrder()
                .getConditionalOrderApplicant2Questions()
                .getSubmittedDate()
                .plusDays(CO_SUBMISSION_DATE_PLUS_DAYS)
                .format(getDateTimeFormatterForPreferredLanguage(data.getApplicant2().getLanguagePreference()))
        );

        notificationService.sendEmail(
            data.getApplicant2EmailAddress(),
            CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER,
            templateVars,
            data.getApplicant2().getLanguagePreference()
        );
    }

}
