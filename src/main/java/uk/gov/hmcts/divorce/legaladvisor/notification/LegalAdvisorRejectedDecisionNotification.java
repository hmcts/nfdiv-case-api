package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.AwaitingClarificationPrinter;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

@Component
@Slf4j
public class LegalAdvisorRejectedDecisionNotification implements ApplicantNotification {

    @Autowired
    private AwaitingClarificationPrinter awaitingClarificationPrinter;

    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        log.info("Notifying applicant 1 offline that their conditional order is rejected: {}", caseId);
        awaitingClarificationPrinter.sendLetter(caseData, caseId, caseData.getApplicant1());
    }

    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 offline that their conditional order is rejected: {}", caseId);
            // TODO: need to generate refusal order that has correct partner content as refusal order generated in previous step is for app 1
            awaitingClarificationPrinter.sendLetter(caseData, caseId, caseData.getApplicant2());
        }
    }
}
