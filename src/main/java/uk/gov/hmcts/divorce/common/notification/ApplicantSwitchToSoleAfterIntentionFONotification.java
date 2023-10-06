package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO;

@Component
@Slf4j
public class ApplicantSwitchToSoleAfterIntentionFONotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && applicantIntendsToSwitchToSole(caseData.getFinalOrder().getApplicant1IntendsToSwitchToSole())) {

            log.info("Notifying Applicant 1 solicitor that they can continue switch to sole for final order {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
                commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && applicantIntendsToSwitchToSole(caseData.getFinalOrder().getApplicant2IntendsToSwitchToSole())) {

            log.info("Notifying Applicant 2 solicitor that they can continue switch to sole for final order {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
                commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && applicantIntendsToSwitchToSole(caseData.getFinalOrder().getApplicant1IntendsToSwitchToSole())) {

            log.info("Notifying Applicant 1 that they can continue switch to sole for final order {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
                commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && applicantIntendsToSwitchToSole(caseData.getFinalOrder().getApplicant2IntendsToSwitchToSole())) {

            log.info("Notifying Applicant 2 that they can continue switch to sole for final order {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO,
                commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private boolean applicantIntendsToSwitchToSole(final Set<FinalOrder.IntendsToSwitchToSole> intendsToSwitchToSoles) {
        return !isEmpty(intendsToSwitchToSoles) && intendsToSwitchToSoles.contains(I_INTEND_TO_SWITCH_TO_SOLE);
    }
}
