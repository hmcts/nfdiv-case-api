package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;

@Component
@Slf4j
public class NoticeOfProceedingsNotification implements ApplicantNotification {

    public static final String CASE_ID = "case id";
    public static final String SOLICITOR_ORGANISATION = "solicitor organisation";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        log.info("Sending Notice Of Proceedings email to applicant.  Case ID: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICANT_NOTICE_OF_PROCEEDINGS,
            commonContent.basicTemplateVars(caseData, caseId),
            caseData.getApplicant1().getLanguagePreference());
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending Notice Of Proceedings email to applicant solicitor.  Case ID: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
            solicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
            ENGLISH);
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending Notice Of Proceedings email to respondent solicitor.  Case ID: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
            respondentSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
            ENGLISH
        );
    }

    private Map<String, String> respondentSolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);
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
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(CASE_ID, caseId.toString());
        return templateVars;
    }
}
