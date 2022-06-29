package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Arrays;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;

@Component
@Slf4j
public class SetSubmitAosState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        State[] aosValidStates = ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived);

        if (Arrays.asList(aosValidStates).contains(caseDetails.getState())) {
            caseDetails.setState(Holding);
            log.info("Setting submit AoS state to Holding for CaseID: {}", caseDetails.getId());
        } else {
            log.info("State not changed for AOS submission task for CaseID: {}", caseDetails.getId());
        }

        return caseDetails;
    }
}
