package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Disputed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@Component
@Slf4j
public class SetSubmitAosState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        if (NO.equals(caseDetails.getData().getAcknowledgementOfService().getJurisdictionAgree())) {
            caseDetails.setState(Disputed);
        } else {
            caseDetails.setState(Holding);
        }

        log.info("Setting submit AoS state: {}, for CaseID: {}", caseDetails.getState(), caseDetails.getId());

        return caseDetails;
    }
}
