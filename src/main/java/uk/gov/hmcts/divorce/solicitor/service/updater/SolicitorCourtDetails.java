package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;

import static uk.gov.hmcts.divorce.divorcecase.model.Court.SERVICE_CENTRE;

@Component
public class SolicitorCourtDetails implements CaseDataUpdater {

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final CaseData caseData = caseDataContext.copyOfCaseData();

        caseData.setDivorceUnit(SERVICE_CENTRE);
        caseData.setSelectedDivorceCentreSiteId(SERVICE_CENTRE.getSiteId());

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(caseData));
    }
}
