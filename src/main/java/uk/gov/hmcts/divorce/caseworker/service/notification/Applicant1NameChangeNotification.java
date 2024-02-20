package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NAME_UPDATED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class Applicant1NameChangeNotification implements ApplicantNotification {
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final String APPLICATION_REFERENCE = "reference number";
    private static final String COURT_EMAIL = "court email";
    private static final String DIVORCE_COURT_EMAIL = "divorceCourtEmail";
    private static final String DISSOLUTION_COURT_EMAIL = "dissolutionCourtEmail";
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailTemplatesConfig config;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info("Notifying applicant about the applicant name update", caseId);

        final Map<String, String> templateVars =
            fillTemplateVars(caseData, caseId, caseData.getApplicant1());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICANT_NAME_UPDATED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> fillTemplateVars(final CaseData caseData,
                                                final Long id,
                                                final Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, id != null ? formatId(id) : null);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(caseData.isDivorce() ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        return templateVars;
    }
}
