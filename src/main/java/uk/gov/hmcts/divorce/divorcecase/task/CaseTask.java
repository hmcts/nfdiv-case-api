package uk.gov.hmcts.divorce.divorcecase.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

public interface CaseTask extends Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> {

    @SafeVarargs
    static CaseTaskRunner caseTasks(final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>>... tasks) {
        return new CaseTaskRunner(of(tasks).reduce(identity(), Function::andThen));
    }
}
