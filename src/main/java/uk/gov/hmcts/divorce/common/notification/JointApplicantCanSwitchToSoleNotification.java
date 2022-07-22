package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;

@Component
@Slf4j
public class JointApplicantCanSwitchToSoleNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()
            && nonNull(caseData.getApplicant1().getEmail())) {
            log.info("Notifying applicant 1 that they can switch to sole: {}", id);

            final Applicant applicant1 = caseData.getApplicant1();

            notificationService.sendEmail(
                applicant1.getEmail(),
                JOINT_APPLICANT_CAN_SWITCH_TO_SOLE,
                commonContent.mainTemplateVars(caseData, id, applicant1, caseData.getApplicant2()),
                applicant1.getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()
            && nonNull(caseData.getApplicant2().getEmail())) {
            log.info("Notifying applicant 2 that they can switch to sole: {}", id);
            final Applicant applicant2 = caseData.getApplicant2();

            notificationService.sendEmail(
                applicant2.getEmail(),
                JOINT_APPLICANT_CAN_SWITCH_TO_SOLE,
                commonContent.mainTemplateVars(caseData, id, applicant2, caseData.getApplicant1()),
                applicant2.getLanguagePreference()
            );
        }
    }
}
