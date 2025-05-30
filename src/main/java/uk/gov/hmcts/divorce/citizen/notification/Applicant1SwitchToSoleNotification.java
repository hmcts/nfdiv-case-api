package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ENDED;

@Component
@Slf4j
@RequiredArgsConstructor
public class Applicant1SwitchToSoleNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending applicant 1 switch to sole notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICANT_SWITCH_TO_SOLE,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (caseData.getApplication().getApplicant2ScreenHasMarriageBroken() != NO) {
            log.info("Sending applicant 1 switch to sole notification to applicant 2 for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                JOINT_APPLICATION_ENDED,
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }
}
