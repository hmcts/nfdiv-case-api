package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class ResetAosFields implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Resetting AOS fields. Case ID: {}", caseDetails.getId());

        caseDetails.getData().getAcknowledgementOfService().setConfirmReadPetition(null);
        caseDetails.getData().getAcknowledgementOfService().setAosIsDrafted(null);

        return caseDetails;
    }
}
