package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;

@Component
@Slf4j
public class LegalAdvisorAmendApplicationDecisionNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 1 solicitor as some amendments needed for case : {}", caseId);

        Applicant applicant = caseData.getApplicant1();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, REJECT),
            ENGLISH
        );

        log.info("Successfully sent CO refused notification to applicant 1 solicitor as some amendments needed for case : {}", caseId);
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 2 solicitor as some amendments needed for case : {}", caseId);

        Applicant applicant = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, REJECT),
            ENGLISH
        );

        log.info("Successfully sent CO refused notification to applicant 2 solicitor as some amendments needed for case : {}", caseId);
    }
}
