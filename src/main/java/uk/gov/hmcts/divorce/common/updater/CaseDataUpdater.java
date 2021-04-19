package uk.gov.hmcts.divorce.common.updater;

public interface CaseDataUpdater {

    CaseDataContext updateCaseData(final CaseDataContext caseDataContext, final CaseDataUpdaterChain caseDataUpdaterChain);
}
