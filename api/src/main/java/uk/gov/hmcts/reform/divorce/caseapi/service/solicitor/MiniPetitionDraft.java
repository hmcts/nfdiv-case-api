package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.util.Handler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

@Component
public class MiniPetitionDraft implements Handler<CaseData> {

    @Override
    public void handle(final CaseData caseData) {
    }
}
