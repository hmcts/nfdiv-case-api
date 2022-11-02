package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;

@Slf4j
@Component
public class HandleWelshFinalOrder implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        log.info("Running HandleWelshFinalOrder task for CaseID {}", details.getId());
        CaseData data = details.getData();

        if (data.isWelshApplication() && isFinalOrderFreeTextPresent(data) && details.getState().equals(FinalOrderRequested)) {
            data.getApplication().setWelshPreviousState(details.getState());
            details.setState(WelshTranslationReview);
            log.info("State set to WelshTranslationReview, WelshPreviousState set to {}, CaseID {}",
                data.getApplication().getWelshPreviousState(), details.getId());
        }

        return details;
    }

    private boolean isFinalOrderFreeTextPresent(CaseData data) {
        return !isEmpty(data.getFinalOrder().getApplicant1FinalOrderLateExplanation())
            || !isEmpty(data.getFinalOrder().getApplicant2FinalOrderLateExplanation());
    }
}
