package uk.gov.hmcts.reform.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.List.of;

public class SearchInputFields implements CcdConfiguration {

    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D_8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String FIRSTNAME = "FirstName";
    public static final String LASTNAME = "LastName";
    public static final String EMAIL = "Email";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        SearchField firstName = SearchField.builder().id(D_8_PETITIONER_FIRST_NAME).label(FIRSTNAME).build();
        SearchField lastName = SearchField.builder().id(D_8_PETITIONER_LAST_NAME).label(LASTNAME).build();
        SearchField email = SearchField.builder().id(D_8_PETITIONER_EMAIL).label(EMAIL).build();

        final List<SearchField> searchFieldList = of(firstName,lastName,email);

        configBuilder.searchInputFields().caseReferenceField();
        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
