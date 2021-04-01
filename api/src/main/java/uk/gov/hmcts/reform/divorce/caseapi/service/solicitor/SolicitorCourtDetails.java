package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.util.Handler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.time.Clock;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.Court.SERVICE_CENTRE;

@Component
public class SolicitorCourtDetails implements Handler<CaseData> {

    @Autowired
    private Clock clock;

    @Override
    public void handle(final CaseData caseData) {

        caseData.setCreatedDate(now(clock));
        caseData.setDivorceUnit(SERVICE_CENTRE);
        caseData.setSelectedDivorceCentreSiteId(SERVICE_CENTRE.getSiteId());
    }
}
