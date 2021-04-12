package uk.gov.hmcts.reform.divorce.caseapi.util;

public interface CaseDataUpdater {

    CaseDataContext updateCaseData(final CaseDataContext caseDataContext, final CaseDataUpdaterChain caseDataUpdaterChain);
}
