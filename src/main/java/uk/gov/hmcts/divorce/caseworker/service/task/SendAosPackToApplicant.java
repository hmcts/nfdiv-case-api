package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SendAosPackToApplicant implements CaseTask {

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplication().isCourtServiceMethod()) {
            log.info("Sending AOS pack to applicant for sole case with id: {}", caseId);
            aosPackPrinter.sendAosLetterToApplicant(caseData, caseId);
        } else if (caseData.getApplication().isPersonalServiceMethod()) {
            log.info("Bulk printing NOP and application pack. Case id: {}:", caseId);
            aosPackPrinter.sendAosLetterAndRespondentAosPackToApplicant(caseData, caseId);
        }

        return caseDetails;
    }
}
