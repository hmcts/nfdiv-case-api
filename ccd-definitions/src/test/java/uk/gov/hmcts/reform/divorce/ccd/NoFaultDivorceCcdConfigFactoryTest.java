package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.divorce.ccd.ccdcase.NoFaultDivorce;
import uk.gov.hmcts.reform.divorce.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.divorce.ccd.event.PatchCase;
import uk.gov.hmcts.reform.divorce.ccd.search.SearchInputFields;
import uk.gov.hmcts.reform.divorce.ccd.search.SearchResultFields;
import uk.gov.hmcts.reform.divorce.ccd.tab.CaseTypeTab;
import uk.gov.hmcts.reform.divorce.ccd.workbasket.WorkBasketInputFields;
import uk.gov.hmcts.reform.divorce.ccd.workbasket.WorkBasketResultFields;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class NoFaultDivorceCcdConfigFactoryTest {

    @Test
    void shouldReturnStreamOfCcdBuilders() {

        final Stream<CcdConfiguration> ccdBuilders = new NoFaultDivorceCcdConfigFactory().getCcdConfig();

        final List<CcdConfiguration> builderList = ccdBuilders.collect(toList());
        assertThat(builderList.size(), is(8));
        assertThat(builderList, contains(
            isA(NoFaultDivorce.class),
            isA(DraftCreate.class),
            isA(PatchCase.class),
            isA(CaseTypeTab.class),
            isA(WorkBasketInputFields.class),
            isA(WorkBasketResultFields.class),
            isA(SearchInputFields.class),
            isA(SearchResultFields.class)));
    }
}