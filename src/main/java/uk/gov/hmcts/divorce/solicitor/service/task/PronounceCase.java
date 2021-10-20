package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
public class PronounceCase implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();

        data.getConditionalOrder().setGrantedDate(data.getConditionalOrder().getDateAndTimeOfHearing().toLocalDate());
        data.getFinalOrder().setDateFinalOrderEligibleFrom(
            data.getConditionalOrder().getDateAndTimeOfHearing().toLocalDate().plusWeeks(6).plusDays(1));
        data.getConditionalOrder().setOutcomeCase(YES);

        // generate document
        // send notification to applicant

        return caseDetails;
    }
}
