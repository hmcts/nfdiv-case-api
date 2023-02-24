package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.print.RegenerateCourtOrdersPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

@Component
@Slf4j
public class RegenerateCourtOrdersNotification implements ApplicantNotification {

    @Autowired
    private RegenerateCourtOrdersPrinter printer;

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 1 for case : {}", caseId);
        printer.print(caseData, caseId, true);
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 2 for case : {}", caseId);
        printer.print(caseData, caseId, false);
    }
}
