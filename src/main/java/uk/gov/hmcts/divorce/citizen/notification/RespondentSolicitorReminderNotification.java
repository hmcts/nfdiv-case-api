package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentSolicitorReminderNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        log.info("Sending the respondent solicitor has not responded notification to the solicitor for case : {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED,
            respondentSolicitorTemplateVars(caseData, caseId),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> respondentSolicitorTemplateVars(CaseData caseData, Long id) {

        Applicant respondent = caseData.getApplicant2();

        var languagePreference = respondent.getLanguagePreference();
        var dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);

        var templateVars = commonContent.basicTemplateVars(caseData, id, languagePreference);

        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(dateTimeFormatter));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(dateTimeFormatter));
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, respondent.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(respondent.getSolicitor().getReference())
                ? respondent.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        return templateVars;
    }
}
