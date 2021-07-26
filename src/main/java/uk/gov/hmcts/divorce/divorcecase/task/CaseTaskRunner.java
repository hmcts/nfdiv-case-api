package uk.gov.hmcts.divorce.divorcecase.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

public final class CaseTaskRunner {

    private final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> caseTask;

    private CaseTaskRunner(final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> caseTask) {
        this.caseTask = caseTask;
    }

    @SafeVarargs
    public static CaseTaskRunner caseTasks(final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>>... tasks) {
        return new CaseTaskRunner(of(tasks).reduce(identity(), Function::andThen));
    }

    public CaseDetails<CaseData, State> run(final CaseDetails<CaseData, State> caseDetails) {
        return caseTask.apply(caseDetails);
    }
}