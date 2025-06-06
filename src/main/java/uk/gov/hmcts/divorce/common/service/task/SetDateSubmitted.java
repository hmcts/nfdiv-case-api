package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetDateSubmitted implements CaseTask {

    private final Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final State state = caseDetails.getState();

        EnumSet<State> submittedStates =
            EnumSet.of(
                Submitted,
                AwaitingDocuments,
                AwaitingRequestedInformation,
                InformationRequested,
                RequestedInformationSubmitted,
                AwaitingHWFDecision
            );

        if (submittedStates.contains(state) || submittedStates.contains(caseData.getApplication().getWelshPreviousState())) {
            caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
            caseData.setDueDate(caseData.getApplication().getDateOfSubmissionResponse());
        }

        return caseDetails;
    }
}
