package uk.gov.hmcts.reform.divorce.caseapi.util;

import java.util.Deque;

public class CaseDataUpdaterChain {

    private final Deque<CaseDataUpdater> caseDataUpdaters;

    public CaseDataUpdaterChain(final Deque<CaseDataUpdater> caseDataUpdaters) {
        this.caseDataUpdaters = caseDataUpdaters;
    }

    public CaseDataContext processNext(final CaseDataContext caseDataContext) {

        if (caseDataUpdaters.isEmpty()) {
            return caseDataContext;
        }

        return caseDataUpdaters.poll().updateCaseData(caseDataContext, this);
    }
}
