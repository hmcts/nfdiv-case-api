package uk.gov.hmcts.reform.divorce.ccd.search;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Search;
import uk.gov.hmcts.reform.divorce.ccd.mock.SearchBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SearchInputFieldsTest {

    private final SearchInputFields searchInputFields = new SearchInputFields();
    private final SearchBuildingMockUtil searchBuildingMockUtil = new SearchBuildingMockUtil().mockSearchBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = searchBuildingMockUtil.getConfigBuilder();
    private final Search.SearchBuilder<CaseData, UserRole> searchBuilder = searchBuildingMockUtil.getSearchBuilder();

    @Test
    public void shouldBuildSearchInputFieldsWithConfigBuilder() {

        searchInputFields.buildWith(configBuilder);

        verify(configBuilder).searchInputFields();
        verify(searchBuilder).caseReferenceField();
        verifyNoMoreInteractions(configBuilder, searchBuilder);
    }
}