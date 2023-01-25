package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;

@Component
@Slf4j
public class SetSubmitAosState implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();

        List<State> applicableStates = Arrays.stream(
                ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived, AwaitingService))
            .filter(state -> !AwaitingConditionalOrder.equals(state)).toList();

        if (applicableStates.contains(caseDetails.getState())) {
            final State state = getState(caseData);
            caseDetails.setState(state);
            log.info("Setting submit AoS state to {} for CaseID: {}", state, caseDetails.getId());
        } else {
            log.info("State not changed for AOS submission task for CaseID: {}", caseDetails.getId());
        }

        if (YES.equals(caseData.getApplicant2().getLanguagePreferenceWelsh())
            || YES.equals(caseData.getApplicant2().getUsedWelshTranslationOnSubmission())) {
            caseData.getApplication().setWelshPreviousState(caseDetails.getState());
            caseDetails.setState(WelshTranslationReview);
            log.info("State set to WelshTranslationReview, WelshPreviousState set to {}, CaseID {}",
                caseData.getApplication().getWelshPreviousState(), caseDetails.getId());
        }

        return caseDetails;
    }

    private State getState(CaseData caseData) {
        boolean isSoleJsApplication = caseData.getApplicationType().isSole()
            && caseData.isJudicialSeparationCase();
        if (isSoleJsApplication) {
            caseData.setAwaitingJsAnswerStartDate(LocalDate.now());
            return caseData.getAcknowledgementOfService().isDisputed() ? AwaitingAnswer : AwaitingJsNullity;
        } else {
            return Holding;
        }
    }
}
