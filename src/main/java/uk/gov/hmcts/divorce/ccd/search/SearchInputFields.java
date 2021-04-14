package uk.gov.hmcts.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.APPLICANT_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.FIRSTNAME;
import static uk.gov.hmcts.divorce.ccd.search.constants.CaseFieldsConstants.LASTNAME;

public class SearchInputFields implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final List<SearchField> searchFieldList = of(
            SearchField.builder().label(FIRSTNAME).id(
                APPLICANT_FIRST_NAME).build(),
            SearchField.builder().label(LASTNAME).id(
                APPLICANT_LAST_NAME).build(),
            SearchField.builder().label(EMAIL).id(APPLICANT_EMAIL).build());

        configBuilder.searchInputFields().caseReferenceField();
        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
