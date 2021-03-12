package uk.gov.hmcts.reform.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.EMAIL;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.FIRSTNAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.SearchInputFieldsConstants.LASTNAME;

public class SearchInputFields implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final List<SearchField> searchFieldList = of(SearchField.builder().label(FIRSTNAME).id(D_8_PETITIONER_FIRST_NAME).build(),
                                                     SearchField.builder().label(LASTNAME).id(D_8_PETITIONER_LAST_NAME).build(),
                                                     SearchField.builder().label(EMAIL).id(D_8_PETITIONER_EMAIL).build());

        configBuilder.searchInputFields().caseReferenceField();
        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
