package uk.gov.hmcts.reform.divorce.ccd;

import uk.gov.hmcts.reform.divorce.ccd.ccdcase.NoFaultDivorce;
import uk.gov.hmcts.reform.divorce.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.divorce.ccd.event.PatchCase;
import uk.gov.hmcts.reform.divorce.ccd.search.SearchInputFields;
import uk.gov.hmcts.reform.divorce.ccd.search.SearchResultFields;
import uk.gov.hmcts.reform.divorce.ccd.tab.CaseTypeTab;
import uk.gov.hmcts.reform.divorce.ccd.workbasket.WorkBasketInputFields;
import uk.gov.hmcts.reform.divorce.ccd.workbasket.WorkBasketResultFields;

import java.util.stream.Stream;

public class NoFaultDivorceCcdConfigFactory implements CcdConfigFactory {

    @Override
    public Stream<CcdConfiguration> getCcdConfig() {
        return Stream.of(
            new NoFaultDivorce(),
            new DraftCreate(),
            new PatchCase(),
            new CaseTypeTab(),
            new WorkBasketInputFields(),
            new WorkBasketResultFields(),
            new SearchInputFields(),
            new SearchResultFields()
        );
    }
}
