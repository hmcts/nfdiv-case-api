package uk.gov.hmcts.reform.divorce.ccd.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Search;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.reform.divorce.ccd.mock.SearchBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SearchInputFieldsTest {

    private final SearchInputFields searchInputFields = new SearchInputFields();
    private final SearchBuildingMockUtil searchBuildingMockUtil = new SearchBuildingMockUtil().mockSearchBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = searchBuildingMockUtil.getConfigBuilder();
    private final Search.SearchBuilder<CaseData, UserRole> searchBuilder = searchBuildingMockUtil.getSearchBuilder();

    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D_8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String FIRSTNAME = "FirstName";
    public static final String LASTNAME = "LastName";
    public static final String EMAIL = "Email";

    @Test
    void shouldBuildSearchInputFieldsWithConfigBuilder() {

        searchInputFields.applyTo(configBuilder);

        final SearchField searchFieldFirstName = SearchField.builder().id(D_8_PETITIONER_FIRST_NAME).label(FIRSTNAME).build();
        final SearchField searchFieldLastName = SearchField.builder().id(D_8_PETITIONER_LAST_NAME).label(LASTNAME).build();
        final SearchField searchFieldEmail = SearchField.builder().id(D_8_PETITIONER_EMAIL).label(EMAIL).build();

        final List<SearchField> searchFieldList = of(searchFieldFirstName,searchFieldLastName,searchFieldEmail);

        verify(searchBuilder).caseReferenceField();
        verify(searchBuilder).fields(searchFieldList);
        verify(configBuilder,times(2)).searchInputFields();
        verifyNoMoreInteractions(configBuilder, searchBuilder);
    }
}
