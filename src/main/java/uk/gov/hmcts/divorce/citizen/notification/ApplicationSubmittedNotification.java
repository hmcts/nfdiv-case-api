package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;

@Component
public class ApplicationSubmittedNotification {

    private static final DateTimeFormatter formatter = ofPattern("d MMMM yyyy");

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();
        commonContent.apply(templateVars, caseData);

        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDateOfSubmissionResponse().format(formatter));

        notificationService.sendEmail(
            caseData.getPetitionerEmail(),
            APPLICATION_SUBMITTED,
            templateVars,
            caseData.getLanguagePreference()
        );
    }
}
