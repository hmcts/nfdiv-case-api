package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER;

@Component
@Slf4j
public class Applicant2CanSwitchToSoleFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 solicitor that they can switch to sole at final order stage for case {}", id);

            final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, id, caseData.getApplicant2());
            templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
            templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
            templateVars.put(PARTNER_IS_REPRESENTED, caseData.getApplicant1().isRepresented() ? "Yes" : "No");

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
                templateVars,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }
}
