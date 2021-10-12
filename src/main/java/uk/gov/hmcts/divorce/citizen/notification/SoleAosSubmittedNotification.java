package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_UNION;

@Component
@Slf4j
public class SoleAosSubmittedNotification {

    private static final String APPLY_FOR_CO_DATE = "apply for CO date";
    private static final String ENDING_YOUR_CIVIL_PARTNERSHIP = "Ending your civil partnership";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicationNotDisputedToApplicant(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(YOUR_UNION, YOUR_DIVORCE);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
        } else {
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
        }

        log.info("Sending Aos submitted without dispute notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOL_APPLICANT_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationNotDisputedToRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(YOUR_UNION, YOUR_DIVORCE.toLowerCase(Locale.ROOT));
        } else {
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP.toLowerCase(Locale.ROOT));
        }

        log.info("Sending Aos submitted without dispute notification to respondent");

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOL_RESPONDENT_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> setTemplateVariables(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, applicant, partner);

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));


        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(PROCESS, DIVORCE_PROCESS);

        } else {
            templateVars.put(PROCESS, CIVIL_PARTNERSHIP_PROCESS);

        }

        return templateVars;
    }
}
