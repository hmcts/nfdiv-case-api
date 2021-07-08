package uk.gov.hmcts.divorce.bulkaction.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class BulkActionSearchResultFieldsTest {
    private BulkActionSearchResultFields searchResultFields;

    @BeforeEach
    void setUp() {
        searchResultFields = new BulkActionSearchResultFields();
    }

    @Test
    void shouldSetSearchInputFields() {
        final Set<BulkActionState> stateSet = Set.of(BulkActionState.class.getEnumConstants());
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder =
            new ConfigBuilderImpl<>(BulkActionCaseData.class, stateSet);

        searchResultFields.configure(configBuilder);

        var searchResultsBuilder = configBuilder.searchResultFields.get(0);
        var searchResults = searchResultsBuilder.build();

        assertThat(searchResults.getFields())
            .extracting("id", "label")
            .contains(
                tuple("[CASE_REFERENCE]", "CCD Case Number")
            );
    }
}
