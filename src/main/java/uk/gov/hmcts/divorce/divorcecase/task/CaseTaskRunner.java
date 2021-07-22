package uk.gov.hmcts.divorce.divorcecase.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.function.Function;

public class CaseTaskRunner {

    private final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> caseTask;

    public CaseTaskRunner(final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> caseTask) {
        this.caseTask = caseTask;
    }

    public CaseDetails<CaseData, State> run(final CaseDetails<CaseData, State> caseDetails) {
        return caseTask.apply(caseDetails);
    }
}