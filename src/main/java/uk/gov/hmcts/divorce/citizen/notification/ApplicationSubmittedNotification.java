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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_PAID;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_JOINT_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class ApplicationSubmittedNotification implements ApplicantNotification {

    private static final String APPLICANT2_NAME = "applicant2 name";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending application submitted notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICATION_SUBMITTED,
            citizenTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending application submitted notification to applicant 2 for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICATION_SUBMITTED,
                citizenTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        String solicitorEmail = caseData.getApplicant1().getSolicitor().getEmail();
        if (!caseData.getApplicationType().isSole() && isNotEmpty(solicitorEmail)) {
            log.info("Sending joint application submitted notification to applicant 1 solicitor for case : {}", caseId);

            notificationService.sendEmail(
                solicitorEmail,
                SOLICITOR_JOINT_APPLICATION_SUBMITTED,
                solicitorTemplateVars(caseData, caseId, caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        String solicitorEmail = caseData.getApplicant2().getSolicitor().getEmail();
        if (!caseData.getApplicationType().isSole() && isNotEmpty(solicitorEmail)) {
            log.info("Sending joint application submitted notification to applicant 2 solicitor for case : {}", caseId);

            notificationService.sendEmail(
                solicitorEmail,
                SOLICITOR_JOINT_APPLICATION_SUBMITTED,
                solicitorTemplateVars(caseData, caseId, caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private Map<String, String> citizenTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(IS_PAID, caseData.getApplication().hasBeenPaidFor() ? YES : NO);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate()
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(APPLICANT2_NAME, caseData.getApplicant2().getFullName());
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());

        return templateVars;
    }
}
