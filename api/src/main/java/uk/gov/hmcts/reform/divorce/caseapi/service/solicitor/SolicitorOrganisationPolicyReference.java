package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

@Component
public class SolicitorOrganisationPolicyReference implements Handler<CaseData> {

    @Override
    public CaseData handle(final CaseData caseData) {
        return caseData;
    }
}
