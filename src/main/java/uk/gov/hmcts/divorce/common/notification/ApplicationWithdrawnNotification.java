package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLICATION_WITHDRAWN;

@Component
@Slf4j
public class ApplicationWithdrawnNotification implements ApplicantNotification {
    private static final String IS_RESPONDENT = "isRespondent";
    private static final String RESPONDENT_PARTNER = "respondentPartner";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        long id = caseDetails.getId();
        CaseData caseData = caseDetails.getData();

        log.info("Sending application withdrawn notification to applicant 1 for: {}", id);
        final Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(IS_RESPONDENT, NO);
        templateVars.put(RESPONDENT_PARTNER, "");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_APPLICATION_WITHDRAWN,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        long id = caseDetails.getId();
        CaseData caseData = caseDetails.getData();

        if (shouldSendNotificationToApplicant2(caseData, caseDetails.getState())) {
            log.info("Sending application withdrawn notification to applicant 2 for: {}", id);
            final Map<String, String> templateVars =
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

            if (caseData.getApplicationType().isSole()) {
                templateVars.put(IS_RESPONDENT, YES);
                templateVars.put(
                    RESPONDENT_PARTNER,
                    commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference())
                );
            } else {
                templateVars.put(IS_RESPONDENT, NO);
                templateVars.put(RESPONDENT_PARTNER, "");
            }

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                CITIZEN_APPLICATION_WITHDRAWN,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    private boolean shouldSendNotificationToApplicant2(final CaseData caseData, final State state) {
        return isNotEmpty(caseData.getApplicant2().getEmail()) &&
            (jointApp2Invited(caseData, state) || soleRespondentInvited(caseData));
    }

    private boolean jointApp2Invited(final CaseData caseData, final State state) {
        return !caseData.getApplicationType().isSole() && !State.Draft.equals(state);
    }

    private boolean soleRespondentInvited(final CaseData caseData) {
        return caseData.getApplicationType().isSole() && !isNull(caseData.getApplication().getIssueDate());
    }
}
