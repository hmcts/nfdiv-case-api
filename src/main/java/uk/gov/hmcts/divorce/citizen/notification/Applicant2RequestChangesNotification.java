package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;

@Component
@Slf4j
public class Applicant2RequestChangesNotification implements ApplicantNotification {

    public static final String APPLICANT_2_COMMENTS = "applicant 2 comments";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICANT_2_COMMENTS, caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation());

        log.info("Sending notification to applicant 1 to request changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending notification to applicant 2 to confirm their request for changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            JOINT_APPLICANT2_REQUEST_CHANGES,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
