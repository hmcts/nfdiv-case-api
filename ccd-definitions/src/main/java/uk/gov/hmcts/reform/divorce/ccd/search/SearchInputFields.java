package uk.gov.hmcts.reform.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SearchInputFields implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchInputFields()
            .caseReferenceField();
    }
}
