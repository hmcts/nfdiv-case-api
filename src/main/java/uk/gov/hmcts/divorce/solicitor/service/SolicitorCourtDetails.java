package uk.gov.hmcts.divorce.solicitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;

import java.time.Clock;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.divorce.ccd.model.enums.Court.SERVICE_CENTRE;

@Component
public class SolicitorCourtDetails implements CaseDataUpdater {

    @Autowired
    private Clock clock;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final CaseData caseData = caseDataContext.copyOfCaseData();

        caseData.setCreatedDate(now(clock));
        caseData.setDivorceUnit(SERVICE_CENTRE);
        caseData.setSelectedDivorceCentreSiteId(SERVICE_CENTRE.getSiteId());

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(caseData));
    }
}
