package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_EMAIL_UPDATED;

@Component
@Slf4j
public class EmailUpdatedNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    public void send(final CaseData caseData, final Long caseId, final String newEmail, final boolean isApplicant1) {

        Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();
        if (applicant.getEmail() != null && !applicant.getEmail().isBlank()) {
            Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
            templateVars.put("old email", applicant.getEmail());
            templateVars.put("new email", newEmail);

            notificationService.sendEmail(
                applicant.getEmail(),
                CITIZEN_EMAIL_UPDATED,
                templateVars,
                applicant.getLanguagePreference(),
                caseId
            );
            log.info("Successfully sent email updated notification for case id: {}", caseId);
        }
    }
}
