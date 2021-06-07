package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;

@Component
@Slf4j
public class ApplicationSentForReviewNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;


    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDateOfSubmissionResponse().format(dateTimeFormatter));
        templateVars.put(APPLICATION_REFERENCE, formatId(id));

        log.info("Sending application awaiting review for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICATION_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
