package uk.gov.hmcts.divorce.util;

public interface CaseDataUpdater {

    CaseDataContext updateCaseData(final CaseDataContext caseDataContext, final CaseDataUpdaterChain caseDataUpdaterChain);
}
