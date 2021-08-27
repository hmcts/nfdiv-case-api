package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;

@Component
@Slf4j
public class AwaitingConditionalOrderNotification {

    @Autowired
    private NotificationService notificationService;

    public void send(CaseData caseData, Long caseId) {

        final String applicant1SolicitorEmail = caseData.getApplicant1().getSolicitor().getEmail();
        final String applicant1SolicitorName = caseData.getApplicant1().getSolicitor().getName();

        final String applicant1FirstName = caseData.getApplicant1().getFirstName();
        final String applicant1LastName = caseData.getApplicant1().getLastName();

        final String applicant2FirstName = caseData.getApplicant2().getFirstName();
        final String applicant2LastName = caseData.getApplicant2().getLastName();

        final YesOrNo languagePreferenceWelsh = caseData.getApplicant1().getLanguagePreferenceWelsh();
        final LanguagePreference languagePreference = languagePreferenceWelsh.equals(NO)
            ? ENGLISH
            : WELSH;

        final Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICANT_NAME, join(" ", applicant1FirstName, applicant1LastName));
        templateVars.put(RESPONDENT_NAME, join(" ", applicant2FirstName, applicant2LastName));
        templateVars.put(SOLICITOR_NAME, applicant1SolicitorName);
        templateVars.put(CCD_REFERENCE, formatId(caseId));

        notificationService.sendEmail(
            applicant1SolicitorEmail,
            SOLICITOR_AWAITING_CONDITIONAL_ORDER,
            templateVars,
            languagePreference
        );

        log.info("Successfully sent awaiting conditional order notification for case : {}", caseId);
    }
}
