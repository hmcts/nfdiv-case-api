package uk.gov.hmcts.divorce.ccd.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.FIRSTNAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.LASTNAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_LAST_NAME;

@Component
public class SearchInputFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final List<SearchField> searchFieldList = of(
            SearchField.builder().label(FIRSTNAME).id(
                PETITIONER_FIRST_NAME).build(),
            SearchField.builder().label(LASTNAME).id(
                PETITIONER_LAST_NAME).build(),
            SearchField.builder().label(EMAIL).id(PETITIONER_EMAIL).build());

        configBuilder.searchInputFields().caseReferenceField();
        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
