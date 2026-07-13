package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SENT_PAPERS_AGAIN;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorSendPapersAgainNotification implements ApplicantNotification {

    private static final String SOLICITOR_SERVICE = "solicitorService";
    private static final String COURT_SERVICE = "courtService";
    public static final String COURT_SERVICE_TEXT = """
     We will now serve the %s again on the respondent. The respondent will have 14 days
     from receiving the papers to respond.""";
    public static final String SOLICITOR_SERVICE_TEXT = """
     You will need to arrange delivery of the %s yourself.You can download the papers from the case and arrange for them to be served on
      the respondent. The documents are usually available in the Documents tab.""";

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (caseData.getApplicationType().isSole()) {

            Applicant applicant = caseData.getApplicant1();
            String divorceOrDissolution = caseData.isDivorce() ? "divorce papers" : "papers to end the civil partnership";
            String divorceOrDissolutionType = caseData.isDivorce() ? "divorce application" : "application to end the civil partnership";

            var templateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);
            templateVars.put(COURT_SERVICE, String.format(COURT_SERVICE_TEXT, divorceOrDissolution));
            templateVars.put(SOLICITOR_SERVICE, String.format(SOLICITOR_SERVICE_TEXT, divorceOrDissolution));
            templateVars.put(DIVORCE_OR_DISSOLUTION, divorceOrDissolutionType);

            log.info("Sending Applicant 1 Solicitor notification informing them that they sent papers again: {}", caseId);

            notificationService.sendEmail(
                applicant.getSolicitor().getEmail(),
                SOLICITOR_SENT_PAPERS_AGAIN,
                templateVars,
                applicant.getLanguagePreference(),
                caseId
            );
        }
    }
}
