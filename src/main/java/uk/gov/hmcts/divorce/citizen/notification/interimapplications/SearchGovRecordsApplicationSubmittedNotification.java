package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED_HWF;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchGovRecordsApplicationSubmittedNotification implements ApplicantNotification {

    @Value("${interim_application.response_offset_days}")
    private long interimApplicationResponseOffsetDays;

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Sending search government records application submitted notification to applicant 1 on case id {}", caseId);

        boolean searchGovRecordsHWF = YesOrNo.YES.equals(caseData.getApplicant1().getInterimApplicationOptions()
            .getInterimAppsUseHelpWithFees());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            searchGovRecordsHWF ? SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED_HWF : SEARCH_GOV_RECORDS_APPLICATION_SUBMITTED,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), searchGovRecordsHWF),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant,
                                             Applicant partner, boolean searchGovRecordsHWF) {

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        if (!searchGovRecordsHWF) {
            DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());
            templateVars.put("date", applicant.getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
                .getApplicationSubmittedDate().plusDays(interimApplicationResponseOffsetDays).format(dateTimeFormatter));
        }

        return templateVars;
    }
}
