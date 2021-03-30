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
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.EMAIL;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.FIRSTNAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.LASTNAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.ccd.search.constants.CaseFieldsConstants.PETITIONER_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class SearchInputFieldsTest {

    private final SearchInputFields searchInputFields = new SearchInputFields();
    private final SearchBuildingMockUtil searchBuildingMockUtil = new SearchBuildingMockUtil().mockSearchBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = searchBuildingMockUtil.getConfigBuilder();
    private final Search.SearchBuilder<CaseData, UserRole> searchBuilder = searchBuildingMockUtil.getSearchBuilder();


    @Test
    void shouldBuildSearchInputFieldsWithConfigBuilder() {

        searchInputFields.applyTo(configBuilder);

        final List<SearchField> searchFieldList = of(
                                                    SearchField.builder().label(FIRSTNAME).id(PETITIONER_FIRST_NAME).build(),
                                                    SearchField.builder().label(LASTNAME).id(PETITIONER_LAST_NAME).build(),
                                                    SearchField.builder().label(EMAIL).id(PETITIONER_EMAIL).build());

        verify(searchBuilder).caseReferenceField();
        verify(searchBuilder).fields(searchFieldList);
        verify(configBuilder,times(2)).searchInputFields();
        verifyNoMoreInteractions(configBuilder, searchBuilder);
    }
}
