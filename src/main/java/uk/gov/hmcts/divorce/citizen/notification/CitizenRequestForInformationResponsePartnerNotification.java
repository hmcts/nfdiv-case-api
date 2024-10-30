package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitizenRequestForInformationResponsePartnerNotification implements ApplicantNotification {

    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID =
        "Sending Request For Information Response Partner Notification to {} for case id: {}";

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, "applicant 1", caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        EmailTemplateName emailTemplateName = YES.equals(latestResponse.getRequestForInformationResponseCannotUploadDocs())
            ? REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS
            : REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            emailTemplateName,
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, "applicant 1 solicitor", caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        EmailTemplateName emailTemplateName = YES.equals(latestResponse.getRequestForInformationResponseCannotUploadDocs())
            ? REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD
            : REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY;

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            emailTemplateName,
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant1(),
                caseData.getApplicant2()
            ),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, "applicant 2", caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        EmailTemplateName emailTemplateName = YES.equals(latestResponse.getRequestForInformationResponseCannotUploadDocs())
            ? REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS
            : REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            emailTemplateName,
            applicantTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, "applicant 2 solicitor", caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        EmailTemplateName emailTemplateName = YES.equals(latestResponse.getRequestForInformationResponseCannotUploadDocs())
            ? REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY_COULD_NOT_UPLOAD
            : REQUEST_FOR_INFORMATION_SOLICITOR_OTHER_PARTY;

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            emailTemplateName,
            solicitorTemplateContent(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1()
            ),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> applicantTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars =
            commonContent.requestForInformationTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private Map<String, String> solicitorTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        LocalDate issueDate = caseData.getApplication().getIssueDate();

        templateVars.put(APPLICANT_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateVars.put(RESPONDENT_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateVars.put(DATE_OF_ISSUE, issueDate != null ? issueDate.format(DATE_TIME_FORMATTER) : "");
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant.getSolicitor().getReference())
            ? applicant.getSolicitor().getReference()
            : "not provided");
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }
}
