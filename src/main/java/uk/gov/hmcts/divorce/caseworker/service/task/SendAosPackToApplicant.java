package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;

@Component
@Slf4j
public class SendAosPackToApplicant implements CaseTask {

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicationType().isSole() && !caseData.getApplicant1().isRepresented()) {

            if (caseData.getApplication().getSolServiceMethod() == PERSONAL_SERVICE) {
                log.info("Bulk printing NOP and application pack for personal service. Case id: {}:", caseId);
                aosPackPrinter.sendPersonalServiceAosLetterToApplicant(caseData, caseId);
            } else {
                log.info("Sending NOP and application pack to bulk print as applicant 1 is not represented. Case id: {}:", caseId);
                aosPackPrinter.sendAosLetterToApplicant(caseData, caseId);
            }
        } else {
            log.info("Not sending NOP and application pack to bulk print as applicant 1 is represented. Case id: {}:", caseId);
        }
        return caseDetails;
    }
}
