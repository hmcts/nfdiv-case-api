package uk.gov.hmcts.divorce.caseworker.service.updater;

import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;

import java.util.function.Function;

public interface CaseDataAction extends Function<CaseDataContext, CaseDataContext> {
}
