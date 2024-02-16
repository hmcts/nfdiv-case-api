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

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Slf4j
@Component
public class SetFinalOrderFieldsAsApplicant2Sol implements CaseTask {

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        log.info("Running SetFinalOrderFieldsAsApplicant2Sol task for CaseID {}", details.getId());
        FinalOrder finalOrder = details.getData().getFinalOrder();

        finalOrder.setApplicant2SolAppliedForFinalOrder(YES);
        finalOrder.setDateApplicant2SolAppliedForFinalOrder(LocalDateTime.now(clock));
        finalOrder.setApplicant2SolResponsibleForFinalOrder(details.getData().getApplicant2().getSolicitor().getName());

        finalOrder.setApplicant2FinalOrderStatementOfTruth(YES);
        finalOrder.setDateFinalOrderSubmitted(LocalDateTime.now(clock));

        return details;
    }
}
