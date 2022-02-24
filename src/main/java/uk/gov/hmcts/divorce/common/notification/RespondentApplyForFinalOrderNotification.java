package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_APPLY_FOR_FINAL_ORDER;

@Component
@Slf4j
public class RespondentApplyForFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        log.info("Notifying respondent that they can apply for a final order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            RESPONDENT_APPLY_FOR_FINAL_ORDER,
            templateVars(caseData, id, applicant2, applicant1),
            applicant2.getLanguagePreference()
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        return commonContent.conditionalOrderTemplateVars(caseData, id, applicant, partner);
    }
}
