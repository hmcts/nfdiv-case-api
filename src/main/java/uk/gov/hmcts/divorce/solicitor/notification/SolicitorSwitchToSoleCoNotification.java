package uk.gov.hmcts.divorce.solicitor.notification;

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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class SolicitorSwitchToSoleCoNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {

        log.info("Notifying solicitor that they made a sole application for a conditional order: {}", id);

        final Applicant applicant = caseData.getApplicant1();

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, id, applicant);
        templateVars.put(APPLICANT_NAME, applicant.getFullName());

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER,
            templateVars,
            applicant.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 2 of partner's CO application as Sole for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            PARTNER_SWITCHED_TO_SOLE_CO,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 2 solicitor that the other party made a sole application for a conditional order: {}", id);

        final Applicant applicant = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, id, applicant);
        templateVars.put(APPLICANT_NAME, applicant.getFullName());

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER,
            templateVars,
            applicant.getLanguagePreference(),
            id
        );
    }
}
