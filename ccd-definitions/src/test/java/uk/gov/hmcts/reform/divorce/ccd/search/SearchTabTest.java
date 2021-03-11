package uk.gov.hmcts.reform.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Search;
import uk.gov.hmcts.reform.divorce.ccd.mock.SearchBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SearchTabTest {

    private final SearchTab searchTab = new SearchTab();
    private final SearchBuildingMockUtil searchBuildingMockUtil = new SearchBuildingMockUtil().mockSearchBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = searchBuildingMockUtil.getConfigBuilder();
    private final Search.SearchBuilder<CaseData, UserRole> searchBuilder = searchBuildingMockUtil.getSearchBuilder();


    @Test
    void shouldBuildSearchTabWithConfigBuilder() {

        searchTab.applyTo(configBuilder);
        // passes
        verify(configBuilder).searchResultFields();
        verify(searchBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("FirstName"));
        verify(searchBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("LastName"));
        verify(searchBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("Email"));

       // fails
        //verify(configBuilder,times(1)).searchResultFields().field(anyString(),any());

        verifyNoMoreInteractions(searchBuilder,configBuilder);
    }
}
