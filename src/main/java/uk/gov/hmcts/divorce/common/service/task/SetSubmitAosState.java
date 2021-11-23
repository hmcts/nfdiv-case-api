package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@Component
@Slf4j
public class SetSubmitAosState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        caseDetails.setState(Holding);

        log.info("Setting submit AoS state to Holding for CaseID: {}", caseDetails.getId());

        return caseDetails;
    }
}
