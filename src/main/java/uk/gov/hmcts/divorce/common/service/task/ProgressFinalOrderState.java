package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;

@Slf4j
@Component
public class ProgressFinalOrderState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        log.info("Running ProgressFinalOrderState task for CaseID {}", details.getId());

        CaseData data = details.getData();
        State state = details.getState();

        var isSole = data.getApplicationType().isSole();
        state = isSole ? FinalOrderRequested : AwaitingFinalOrder.equals(state)
                ? AwaitingJointFinalOrder
                : FinalOrderRequested;

        details.setData(data);
        details.setState(state);
        return details;
    }
}
