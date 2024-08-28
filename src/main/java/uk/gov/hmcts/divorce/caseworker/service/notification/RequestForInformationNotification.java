package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_YET_ISSUED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.REQUEST_FOR_INFORMATION_DETAILS;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_JOINT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_JOINT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_OTHER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLE_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestForInformationNotification implements ApplicantNotification {

    public static final String REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Sole Notification to {} for case id: {}";

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID,
            caseData.getApplicationType().isSole() ? "applicant solicitor" : "applicant 1 solicitor", caseId);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        EmailTemplateName emailTemplate = caseData.getApplicationType().isSole()
            ? REQUEST_FOR_INFORMATION_SOLE_SOLICITOR
            : REQUEST_FOR_INFORMATION_JOINT_SOLICITOR;

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            emailTemplate,
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2(),
                requestForInformation.getRequestForInformationDetails()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID,
            caseData.getApplicationType().isSole() ? "applicant" : "applicant 1", caseId);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        EmailTemplateName emailTemplate = caseData.getApplicationType().isSole()
            ? REQUEST_FOR_INFORMATION_SOLE
            : REQUEST_FOR_INFORMATION_JOINT;

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            emailTemplate,
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2(),
                requestForInformation.getRequestForInformationDetails()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID, "applicant 2", caseId);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            REQUEST_FOR_INFORMATION_JOINT,
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1(),
                requestForInformation.getRequestForInformationDetails()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID, "applicant 2 solicitor", caseId);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            REQUEST_FOR_INFORMATION_JOINT_SOLICITOR,
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1(),
                requestForInformation.getRequestForInformationDetails()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToOtherRecipient(final CaseData caseData, final Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_SOLE_NOTIFICATION_TO_FOR_CASE_ID, "other recipient", caseId);

        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        notificationService.sendEmail(
            requestForInformation.getRequestForInformationEmailAddress(),
            REQUEST_FOR_INFORMATION_OTHER,
            otherRecipientTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), requestForInformation),
            LanguagePreference.ENGLISH,
            caseId
        );
    }

    private Map<String, String> solicitorTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner,
                                                         final String requestForInformationDetails) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        LocalDate issueDate = caseData.getApplication().getIssueDate();

        templateVars.put(APPLICANT_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(DATE_OF_ISSUE, issueDate != null ? issueDate.format(DATE_TIME_FORMATTER) : "");
        templateVars.put(ISSUE_DATE_POPULATED, issueDate != null ? YES : NO);
        templateVars.put(NOT_YET_ISSUED, issueDate == null ? YES : NO);
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant.getSolicitor().getReference())
            ? applicant.getSolicitor().getReference()
            : "not provided");
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, requestForInformationDetails);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private Map<String, String> applicantTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner,
                                                         final String requestForInformationDetails) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, requestForInformationDetails);
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private Map<String, String> otherRecipientTemplateContent(final CaseData caseData,
                                                              final Long caseId,
                                                              final Applicant applicant,
                                                              final Applicant partner,
                                                              final RequestForInformation requestForInformation) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(APPLICANT_NAME, caseData.getApplicant1().getFullName());
        templateVars.put(RESPONDENT_NAME, caseData.getApplicant2().getFullName());
        templateVars.put(RECIPIENT_NAME, requestForInformation.getRequestForInformationName());
        templateVars.put(ISSUE_DATE_POPULATED, caseData.getApplication().getIssueDate() != null ? YES : NO);
        templateVars.put(NOT_YET_ISSUED, caseData.getApplication().getIssueDate() == null ? YES : NO);
        templateVars.put(REQUEST_FOR_INFORMATION_DETAILS, requestForInformation.getRequestForInformationDetails());
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }
}
