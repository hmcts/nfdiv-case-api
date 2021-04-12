package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdater;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChain;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.time.Clock;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.Court.SERVICE_CENTRE;

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
