package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CASE_ID;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_ORGANISATION;

@Component
@Slf4j
public class NoticeOfProceedingsNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId) {

        final Applicant applicant = caseData.getApplicant1();
        final Applicant respondent = caseData.getApplicant2();
        final Solicitor applicantSolicitor = applicant.getSolicitor();
        final Solicitor respondentSolicitor = respondent.getSolicitor();

        if (respondent.isRepresented() && respondentSolicitor.hasDigitalDetails()) {

            log.info("Sending Notice Of Proceedings email to respondent solicitor.  Case ID: {}", caseId);

            notificationService.sendEmail(
                respondentSolicitor.getEmail(),
                RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                respondentSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH
            );
        }

        if (applicant.isRepresented()) {

            log.info("Sending Notice Of Proceedings email to applicant solicitor.  Case ID: {}", caseId);

            notificationService.sendEmail(
                applicantSolicitor.getEmail(),
                APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                solicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH);
        } else {

            log.info("Sending Notice Of Proceedings email to applicant.  Case ID: {}", caseId);

            notificationService.sendEmail(
                applicant.getEmail(),
                APPLICANT_NOTICE_OF_PROCEEDINGS,
                commonContent.commonNotificationTemplateVars(caseData, caseId),
                applicant.getLanguagePreference());
        }
    }

    private Map<String, String> respondentSolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData,
                                                                                   final Long caseId) {

        final Map<String, String> templateVars = commonContent.commonNotificationTemplateVars(caseData, caseId);
        final Solicitor respondentSolicitor = caseData.getApplicant2().getSolicitor();
        final String respondentOrganisationName = respondentSolicitor
            .getOrganisationPolicy()
            .getOrganisation()
            .getOrganisationName();

        templateVars.put(SOLICITOR_NAME, respondentSolicitor.getName());
        templateVars.put(CASE_ID, caseId.toString());
        templateVars.put(SOLICITOR_ORGANISATION, respondentOrganisationName);

        return templateVars;
    }

    private Map<String, String> solicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = commonContent.commonNotificationTemplateVars(caseData, caseId);

        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(CASE_ID, caseId.toString());

        return templateVars;
    }
}
