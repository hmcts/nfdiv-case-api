package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CASE_ID;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;

@Component
public class NoticeOfProceedingsNotification {

    @Autowired
    private NotificationService notificationService;

    public void sendToApplicantOrSolicitor(final CaseData caseData, final Long caseId) {

        final Applicant applicant = caseData.getApplicant1();
        final Solicitor applicantSolicitor = applicant.getSolicitor();

        if (isApplicantRepresented(applicantSolicitor)) {

            //Send to applicant solicitor
            notificationService.sendEmail(
                applicantSolicitor.getEmail(),
                APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                solicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH);
        }

        //Send to applicant
        notificationService.sendEmail(
            applicant.getEmail(),
            APPLICANT_NOTICE_OF_PROCEEDINGS,
            commonNoticeOfProceedingsTemplateVars(caseData, caseId),
            applicant.getLanguagePreference());
    }

    private boolean isApplicantRepresented(final Solicitor applicantSolicitor) {
        return null != applicantSolicitor && isNotEmpty(applicantSolicitor.getEmail());
    }

    private Map<String, String> solicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = commonNoticeOfProceedingsTemplateVars(caseData, caseId);

        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(CASE_ID, caseId.toString());

        return templateVars;
    }

    private Map<String, String> commonNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = new HashMap<>();
        final Applicant applicant = caseData.getApplicant1();
        final Applicant respondent = caseData.getApplicant2();

        templateVars.put(APPLICANT_NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateVars.put(RESPONDENT_NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));

        return templateVars;
    }
}
