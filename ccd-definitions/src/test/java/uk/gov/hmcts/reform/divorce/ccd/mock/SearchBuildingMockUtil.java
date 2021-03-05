package uk.gov.hmcts.reform.divorce.ccd.mock;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Search;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Getter
public class SearchBuildingMockUtil {

    private ConfigBuilder<CaseData, State, UserRole> configBuilder;
    private Search.SearchBuilder<CaseData, UserRole> searchBuilder;

    @SuppressWarnings("unchecked")
    public SearchBuildingMockUtil mockSearchBuildingWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        this.configBuilder = configBuilder;
        searchBuilder = mock(Search.SearchBuilder.class, withSettings().lenient());

        when(configBuilder.searchInputFields()).thenReturn(searchBuilder);
        when(configBuilder.searchResultFields()).thenReturn(searchBuilder);

        return this;
    }

    @SuppressWarnings("unchecked")
    public SearchBuildingMockUtil mockSearchBuilding() {
        return mockSearchBuildingWith(mock(ConfigBuilder.class, withSettings().lenient()));
    }

    @SuppressWarnings("unchecked")
    private Search.SearchBuilder<CaseData, UserRole> createMockSearchBuilder() {

        final SearchBuilderAnswer searchBuilderAnswer = new SearchBuilderAnswer();
        final Search.SearchBuilder<CaseData, UserRole> searchBuilder = mock(Search.SearchBuilder.class, searchBuilderAnswer);
        searchBuilderAnswer.setBuilderAnswer(searchBuilder);

        return searchBuilder;
    }
}
