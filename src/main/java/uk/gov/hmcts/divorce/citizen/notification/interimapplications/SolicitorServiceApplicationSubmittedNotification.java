package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SERVICE_APPLICATION_SUBMITTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorServiceApplicationSubmittedNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        log.info("Sending service application submitted notification to applicant1 Solicitor1 on case id {}", caseId);

        final Map<String, String> templateVars =
            commonContent.solicitorTemplateVarsPreIssue(caseData, caseId, caseData.getApplicant1());

        addApplicantLabelAndIssueDateVars(templateVars, caseData, caseData.getApplicant1());

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLICITOR_SERVICE_APPLICATION_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private void addApplicantLabelAndIssueDateVars(Map<String, String> templateVars, CaseData caseData, Applicant applicant1) {

        AlternativeServiceType serviceType = caseData.getAlternativeService().getAlternativeServiceType();

        templateVars.put(DATE_OF_ISSUE, commonContent.getIssueDateInPreferredLanguage(caseData, applicant1));
        commonContent.addServiceApplicationTypeVars(templateVars, serviceType);

    }
}
