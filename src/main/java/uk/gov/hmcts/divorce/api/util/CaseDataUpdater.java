package uk.gov.hmcts.divorce.api.util;

public interface CaseDataUpdater {

    CaseDataContext updateCaseData(final CaseDataContext caseDataContext, final CaseDataUpdaterChain caseDataUpdaterChain);
}
