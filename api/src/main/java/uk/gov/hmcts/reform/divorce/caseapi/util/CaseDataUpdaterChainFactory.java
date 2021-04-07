package uk.gov.hmcts.reform.divorce.caseapi.util;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class CaseDataUpdaterChainFactory {

    public CaseDataUpdaterChain createWith(final List<CaseDataUpdater> caseDataUpdaters) {
        return new CaseDataUpdaterChain(new LinkedList<>(caseDataUpdaters));
    }
}
