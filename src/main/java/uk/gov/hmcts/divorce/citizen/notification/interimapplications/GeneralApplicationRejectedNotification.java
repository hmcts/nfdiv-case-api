package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_REJECTED_BY_CASEWORKER;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationRejectedNotification {

    public static final String IS_SEARCH_GOV_RECORDS = "isSearchGovRecords";
    public static final String IS_OTHER_D11_APP = "isOtherD11App";

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId, final boolean isApplicant1, final boolean isSearchGovRecords) {
        log.info(
            "Sending general application rejected notification to {} on case id {}", isApplicant1 ? "applicant 1" : "applicant 2", caseId);
        Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(IS_SEARCH_GOV_RECORDS, isSearchGovRecords ? YES : NO);
        templateVars.put(IS_OTHER_D11_APP, !isSearchGovRecords ? YES : NO);

        notificationService.sendEmail(
            applicant.getEmail(),
            GENERAL_APPLICATION_REJECTED_BY_CASEWORKER,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
