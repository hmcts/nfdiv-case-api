package uk.gov.hmcts.reform.divorce.ccd.mock;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Search;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class SearchBuildingMockUtil {

    private ConfigBuilder<CaseData, State, UserRole> configBuilder;
    private Search.SearchBuilder<CaseData, UserRole> searchBuilder;

    @SuppressWarnings("unchecked")
    public SearchBuildingMockUtil mockSearchBuilding() {

        configBuilder = mock(ConfigBuilder.class);
        searchBuilder = mock(Search.SearchBuilder.class);

        when(configBuilder.searchInputFields()).thenReturn(searchBuilder);
        when(configBuilder.searchResultFields()).thenReturn(searchBuilder);

        return this;
    }

    private Search.SearchBuilder<CaseData, UserRole> createMockSearchBuilder() {

        final SearchBuilderAnswer searchBuilderAnswer = new SearchBuilderAnswer();
        final Search.SearchBuilder<CaseData, UserRole> searchBuilder = mock(Search.SearchBuilder.class, searchBuilderAnswer);
        searchBuilderAnswer.setBuilderAnswer(searchBuilder);

        return searchBuilder;
    }
}
