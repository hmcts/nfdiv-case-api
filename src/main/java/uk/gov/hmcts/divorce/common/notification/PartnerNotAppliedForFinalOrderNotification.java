package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification.PARTNER_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER;

@Component
@Slf4j
public class PartnerNotAppliedForFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole() && YES.equals(caseData.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {

            log.info("Notifying Applicant 1 that partner has not applied for final order for case {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER,
                commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole() && YES.equals(caseData.getFinalOrder().getApplicant2AppliedForFinalOrderFirst())) {

            log.info("Notifying Applicant 2 that partner has not applied for final order for case {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER,
                commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole() && YES.equals(caseData.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {

            log.info("Notifying Applicant 1 solicitor that other applicant has not applied for final order for case {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
                getSolicitorTemplateVars(caseData, caseId, caseData.getApplicant1()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole() && YES.equals(caseData.getFinalOrder().getApplicant2AppliedForFinalOrderFirst())) {

            log.info("Notifying Applicant 2 solicitor that other applicant has not applied for final order for case {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
                getSolicitorTemplateVars(caseData, caseId, caseData.getApplicant2()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private Map<String, String> getSolicitorTemplateVars(final CaseData caseData, final Long caseId, Applicant applicant) {

        Map<String, String> solicitorTemplateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);

        solicitorTemplateVars.put(IS_DIVORCE, caseData.isDivorce() ? CommonContent.YES : NO);
        solicitorTemplateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? CommonContent.YES : NO);
        solicitorTemplateVars.put(PARTNER_IS_REPRESENTED, caseData.getApplicant2().isRepresented() ? CommonContent.YES : NO);

        return solicitorTemplateVars;
    }
}
