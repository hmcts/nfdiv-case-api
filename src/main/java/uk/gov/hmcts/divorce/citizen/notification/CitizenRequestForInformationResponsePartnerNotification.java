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

import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;

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

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            getEmailTemplateName(latestResponse),
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
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        log.info(REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_TO_FOR_CASE_ID, "applicant 2", caseId);

        RequestForInformationResponse latestResponse =
            caseData.getRequestForInformationList().getLatestRequest().getLatestResponse();

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            getEmailTemplateName(latestResponse),
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

    private Map<String, String> applicantTemplateContent(final CaseData caseData,
                                                         final Long caseId,
                                                         final Applicant applicant,
                                                         final Applicant partner) {
        Map<String, String> templateVars =
            commonContent.requestForInformationTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(SMART_SURVEY, commonContent.getSmartSurvey());

        return templateVars;
    }

    private EmailTemplateName getEmailTemplateName(RequestForInformationResponse requestForInformationResponse) {
        if (requestForInformationResponse.isOffline()) {
            return YES.equals(requestForInformationResponse.getRfiOfflineResponseAllDocumentsUploaded())
                ? REQUEST_FOR_INFORMATION_RESPONSE_PARTNER
                : REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS;
        } else {
            return YES.equals(requestForInformationResponse.getRequestForInformationResponseCannotUploadDocs())
                ? REQUEST_FOR_INFORMATION_RESPONSE_CANNOT_UPLOAD_DOCS
                : REQUEST_FOR_INFORMATION_RESPONSE_PARTNER;
        }
    }
}
