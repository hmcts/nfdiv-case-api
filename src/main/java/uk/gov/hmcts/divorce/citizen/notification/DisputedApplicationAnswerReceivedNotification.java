package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Objects;

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_APPLICANT1_DISPUTE_ANSWER_RECEIVED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class DisputedApplicationAnswerReceivedNotification implements ApplicantNotification {

    static final String ISSUE_DATE = "date of issue";

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        if (caseData.getApplicationType().isSole()) {
            log.info("Notifying Applicant1's Solicitor that an Answer has been received from the respondent");

            Applicant applicant = caseData.getApplicant1();
            var dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());

            var templateVars = commonContent.basicTemplateVars(caseData, id, applicant.getLanguagePreference());
            templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
            templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
            templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(dateTimeFormatter));
            templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

            var solicitor = caseData.getApplicant1().getSolicitor();
            templateVars.put(SOLICITOR_NAME, solicitor.getName());
            templateVars.put(SOLICITOR_REFERENCE, Objects.nonNull(solicitor.getReference()) ? solicitor.getReference() : "not provided");

            notificationService.sendEmail(
                solicitor.getEmail(),
                SOLICITOR_APPLICANT1_DISPUTE_ANSWER_RECEIVED,
                templateVars,
                applicant.getLanguagePreference(),
                id
            );
        }
    }
}
