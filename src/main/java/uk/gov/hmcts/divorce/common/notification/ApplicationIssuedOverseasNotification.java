package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrPostalAddress.CONTACT_DETAILS_UPDATED;
import static uk.gov.hmcts.divorce.notification.CommonContent.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APP1_UPDATED_PARTNER_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationIssuedOverseasNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        log.info("Notifying sole applicant of application issue (case {}) to overseas respondent", id);

        boolean isContactDetailsUpdated = Optional.of(caseData.getApplicant1())
            .map(Applicant::getInterimApplicationOptions)
            .map(InterimApplicationOptions::getNoResponseJourneyOptions)
            .map(NoResponseJourneyOptions::getNoResponsePartnerNewEmailOrPostalAddress)
            .map(CONTACT_DETAILS_UPDATED::equals)
            .orElse(false);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            isContactDetailsUpdated ? APP1_UPDATED_PARTNER_CONTACT_DETAILS : OVERSEAS_RESPONDENT_APPLICATION_ISSUED,
            overseasRespondentTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    private Map<String, String> overseasRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonContent.mainTemplateVars(
            caseData,
            id,
            caseData.getApplicant1(),
            caseData.getApplicant2());
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(28)
                .format(getDateTimeFormatterForPreferredLanguage(caseData.getApplicant1().getLanguagePreference())));
        return templateVars;
    }
}
