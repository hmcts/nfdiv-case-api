package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.SwitchToSoleSolicitorTemplateContent;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR;

@Slf4j
@Component
public class SolicitorIntendsToSwitchToSoleFoNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    SwitchToSoleSolicitorTemplateContent solicitorTemplateContent;

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant2IntendToSwitchToSole())) {
            log.info("Notifying applicant 1 solicitor that other applicant intends to switch to sole fo : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
                solicitorTemplateContent.templatevars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant1IntendToSwitchToSole())) {
            log.info("Notifying applicant 2 solicitor that other applicant intends to switch to sole fo : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
                solicitorTemplateContent.templatevars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant1IntendToSwitchToSole())) {
            log.info("Notifying applicant 2 that other applicant intends to switch to sole fo : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN,
                solicitorTemplateContent.templatevars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }
}
