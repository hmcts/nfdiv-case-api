package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;

@Slf4j
@Component
public class SetFinalOrderFieldsAsApplicant2 implements CaseTask {

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        log.info("Running SetFinalOrderFieldsAsApplicant2 task for CaseID {}", details.getId());

        if (AwaitingFinalOrder.equals(details.getState())) {
            log.info("Updating final order fields for CaseID {} (SetFinalOrderFieldsAsApplicant2)", details.getId());
            FinalOrder finalOrder = details.getData().getFinalOrder();

            if (isNull(finalOrder.getApplicant1AppliedForFinalOrderFirst())
                && isNull(finalOrder.getApplicant2AppliedForFinalOrderFirst())) {
                finalOrder.setApplicant2AppliedForFinalOrderFirst(YES);
                finalOrder.setApplicant1AppliedForFinalOrderFirst(NO);
                finalOrder.setDateFinalOrderSubmitted(LocalDateTime.now(clock));
            }
        }

        return details;
    }
}
