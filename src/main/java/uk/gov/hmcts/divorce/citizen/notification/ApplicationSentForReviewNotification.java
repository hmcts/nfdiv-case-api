package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;

@Component
@Slf4j
public class ApplicationSentForReviewNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put("date plus two weeks", caseData.getDueDate().format(ofPattern("d MMMM yyyy")));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(DIVORCE_OR_DISSOLUTION, "divorce application");
            templateVars.put(" civilpartnership.case@justice .gov.uk/contactdivorce@justice.gov.uk", "contactdivorce@justice.gov.uk");
            if (caseData.getApplicant2().getGender().equals(Gender.MALE)) {
                templateVars.put(PARTNER, "husband");
            } else {
                templateVars.put(PARTNER, "wife");
            }
        } else {
            templateVars.put(DIVORCE_OR_DISSOLUTION, "application to end your civil partnership");
            templateVars.put(" civilpartnership.case@justice .gov.uk/contactdivorce@justice.gov.uk", "civilpartnership.case@justice.gov.uk");
            templateVars.put(PARTNER, "civil partner");
        }

        log.info("Sending application awaiting review for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
