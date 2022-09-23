package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

@Component
@Slf4j
public class RegenerateCourtOrdersNotification implements ApplicantNotification {

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 1 for case : {}", caseId);

    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 2 for case : {}", caseId);

    }
}
