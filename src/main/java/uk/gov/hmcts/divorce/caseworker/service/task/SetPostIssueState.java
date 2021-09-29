package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@Component
@Slf4j
public class SetPostIssueState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Application application = caseDetails.getData().getApplication();

        if (!caseDetails.getData().getApplicationType().isSole()) {
            caseDetails.setState(Holding);
        } else if (application.isSolicitorApplication() && application.isSolicitorServiceMethod()) {
            caseDetails.setState(AwaitingService);
        } else {
            caseDetails.setState(AwaitingAos);
        }

        log.info("Setting state to {}.  Case ID: {}", caseDetails.getState(), caseDetails.getId());
        return caseDetails;
    }
}
