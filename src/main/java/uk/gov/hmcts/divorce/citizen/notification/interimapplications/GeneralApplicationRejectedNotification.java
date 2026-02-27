package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_REJECTED_BY_CASEWORKER;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationRejectedNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId, final boolean isApplicant1) {
        log.info(
            "Sending general application rejected notification to {} on case id {}", isApplicant1 ? "applicant 1" : "applicant 2", caseId);
        Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        notificationService.sendEmail(
            applicant.getEmail(),
            GENERAL_APPLICATION_REJECTED_BY_CASEWORKER,
            commonContent.mainTemplateVars(caseData, caseId, applicant, partner),
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
