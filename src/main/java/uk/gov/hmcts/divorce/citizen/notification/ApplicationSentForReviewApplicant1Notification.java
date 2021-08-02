package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;

@Component
@Slf4j
public class ApplicationSentForReviewApplicant1Notification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(caseData, caseData.getApplicant1());

        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(dateTimeFormatter));

        log.info("Sending application sent for review notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
