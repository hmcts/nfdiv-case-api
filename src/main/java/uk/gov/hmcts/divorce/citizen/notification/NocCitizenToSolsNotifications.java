package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Optional;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_NEW_SOL;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL;


@RequiredArgsConstructor
@Component
@Slf4j
public class NocCitizenToSolsNotifications implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;


    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app1 : {}", id);
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN,
            commonContent.nocCitizenTemplateVars(id, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app2 : {}", id);
        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN,
            commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app1Solicitor : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            NOC_TO_SOLS_EMAIL_NEW_SOL,
            commonContent.nocSolsTemplateVars(id, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }


    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app2Solicitor : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            NOC_TO_SOLS_EMAIL_NEW_SOL,
            commonContent.nocSolsTemplateVars(id, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC notification to app1OldSolicitor : {}", id);
        Optional.ofNullable(oldCaseData)
            .map(CaseData::getApplicant1)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_OLD_SOL,
                commonContent.nocOldSolsTemplateVars(id, oldCaseData.getApplicant1()),
                oldCaseData.getApplicant1().getLanguagePreference(),
                id
            ));
    }

    @Override
    public void sendToApplicant2OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC notification to app2OldSolicitor : {}", id);

        Optional.ofNullable(oldCaseData)
            .map(CaseData::getApplicant2)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_OLD_SOL,
                commonContent.nocOldSolsTemplateVars(id, oldCaseData.getApplicant2()),
                oldCaseData.getApplicant2().getLanguagePreference(),
                id
            ));
    }

}
