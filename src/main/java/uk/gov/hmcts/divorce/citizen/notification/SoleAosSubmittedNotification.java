package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_UNION;

@Component
@Slf4j
public class SoleAosSubmittedNotification {

    private static final String APPLY_FOR_CO_DATE = "apply for CO date";
    private static final String ENDING_OF_YOUR_CIVIL_PARTNERSHIP = "ending of your civil partnership";
    private static final String ENDING_OF_YOUR_UNION = "ending of your union";
    private static final String DIVORCE_OR_DISSOLUTION = "divorce / dissolution";
    private static final String SERVICE = "service";
    private static final String DIVORCE_SERVICE = "Divorce Service";
    private static final String CIVIL_PARTNERSHIP_SERVICE = "Ending Civil Partnerships";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicationNotDisputedToApplicant(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(APPLICATION_REFERENCE, formatId(id));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(YOUR_UNION, YOUR_DIVORCE);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
            templateVars.put(PROCESS, DIVORCE_PROCESS);
        } else {
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
            templateVars.put(PROCESS, CIVIL_PARTNERSHIP_PROCESS);
        }

        log.info("Sending Aos submitted without dispute notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationNotDisputedToRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(YOUR_UNION, YOUR_DIVORCE);
        } else {
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP.toLowerCase(Locale.ROOT));
        }

        log.info("Sending Aos submitted without dispute notification to respondent");

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    public void sendApplicationDisputedToApplicant(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(YOUR_UNION, YOUR_DIVORCE.toLowerCase(Locale.ROOT));
            templateVars.put(ENDING_OF_YOUR_UNION, DIVORCE);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
        } else {
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP.toLowerCase(Locale.ROOT));
            templateVars.put(ENDING_OF_YOUR_UNION, ENDING_OF_YOUR_CIVIL_PARTNERSHIP);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
        }

        log.info("Sending Aos disputed notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationDisputedToRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(DIVORCE_OR_DISSOLUTION, DIVORCE);
            templateVars.put(SERVICE, DIVORCE_SERVICE);
            templateVars.put(ENDING_OF_YOUR_UNION, DIVORCE);
        } else {
            templateVars.put(DIVORCE_OR_DISSOLUTION, DISSOLUTION);
            templateVars.put(SERVICE, CIVIL_PARTNERSHIP_SERVICE);
            templateVars.put(ENDING_OF_YOUR_UNION, APPLICATION_TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending Aos submitted disputed notification to respondent");

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
