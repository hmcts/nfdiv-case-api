package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.print.FinalOrderGrantedPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANTS_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class FinalOrderGrantedNotification implements ApplicantNotification {

    public static final String FINAL_ORDER_GRANTED_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Final Order Granted Notification to {} for case id: {}";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private FinalOrderGrantedPrinter printer;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        log.info(FINAL_ORDER_GRANTED_NOTIFICATION_TO_FOR_CASE_ID,
            caseData.getApplicationType().isSole() ? "applicant solicitor" : "applicant 1 solicitor", caseId);
        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLICITOR_FINAL_ORDER_GRANTED,
            solicitorTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        log.info("Sending Final Order Granted letter to Applicant 1: {}", caseId);
        printer.print(caseData, caseId, FINAL_ORDER_GRANTED_COVER_LETTER_APP_1);
    }

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info(FINAL_ORDER_GRANTED_NOTIFICATION_TO_FOR_CASE_ID, "applicant", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                APPLICANTS_FINAL_ORDER_GRANTED,
                citizenTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        log.info("Sending Final Order Granted letter to Applicant 2: {}", caseId);
        printer.print(caseData, caseId, FINAL_ORDER_GRANTED_COVER_LETTER_APP_2);
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info(FINAL_ORDER_GRANTED_NOTIFICATION_TO_FOR_CASE_ID, "respondent", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                APPLICANTS_FINAL_ORDER_GRANTED,
                citizenTemplateContent(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        log.info(FINAL_ORDER_GRANTED_NOTIFICATION_TO_FOR_CASE_ID,
            caseData.getApplicationType().isSole() ? "respondent solicitor" : "applicant 2 solicitor", caseId);
        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLICITOR_FINAL_ORDER_GRANTED,
            solicitorTemplateContent(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> solicitorTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, caseId, applicant, partner);

        templateVars.put(APPLICANT_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant.getSolicitor().getReference())
            ? applicant.getSolicitor().getReference()
            : "not provided");
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));

        return templateVars;
    }

    private Map<String, String> citizenTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, caseId, applicant, partner);

        //TODO: temporarily set to false, need to update when Final Order switch to sole journey is being developed
        templateVars.put("isSwitchedToSolePartner", "false");

        return templateVars;
    }
}
