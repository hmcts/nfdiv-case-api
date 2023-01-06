package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@Component
@Slf4j
public class SetConfirmServiceState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();

        if (caseData.getApplicationType().isSole() && caseData.getAcknowledgementOfService().getDateAosSubmitted() != null) {
            log.info("Skip Setting State for Sole Case: AoS previously Submitted for CaseId: {}", caseDetails.getId());
            return caseDetails;
        } else {
            if (!isEmpty(caseData.getApplication().getSolicitorService().getServiceProcessedByProcessServer())) {
                log.info("Setting State: Holding for CaseId: {}", caseDetails.getId());
                caseDetails.setState(Holding);
            } else {
                caseDetails.setState(AwaitingAos);
            }
        }

        return caseDetails;
    }
}
