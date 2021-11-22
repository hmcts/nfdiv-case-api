package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class SoleAosSubmittedNotification {

    private static final String APPLY_FOR_CO_DATE = "apply for CO date";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicationNotDisputedToApplicant(CaseData caseData, Long id) {
        log.info("Sending Aos submitted without dispute notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_AOS_SUBMITTED,
            nonDisputedTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationNotDisputedToRespondent(CaseData caseData, Long id) {
        log.info("Sending Aos submitted without dispute notification to respondent");

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_AOS_SUBMITTED,
            nonDisputedTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> nonDisputedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
