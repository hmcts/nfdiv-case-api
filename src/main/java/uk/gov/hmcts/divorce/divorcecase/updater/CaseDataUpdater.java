package uk.gov.hmcts.divorce.divorcecase.updater;

public interface CaseDataUpdater {

    CaseDataContext updateCaseData(final CaseDataContext caseDataContext, final CaseDataUpdaterChain caseDataUpdaterChain);
}
