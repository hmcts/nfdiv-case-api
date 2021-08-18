package uk.gov.hmcts.divorce.bulkaction.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getSearchResultFields;

public class BulkActionSearchResultFieldsTest {
    private BulkActionSearchResultFields searchResultFields;

    @BeforeEach
    void setUp() {
        searchResultFields = new BulkActionSearchResultFields();
    }

    @Test
    void shouldSetSearchInputFields() throws Exception {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        searchResultFields.configure(configBuilder);

        assertThat(getSearchResultFields(configBuilder).getFields())
            .extracting("id", "label")
            .contains(
                tuple("[CASE_REFERENCE]", "Case Number")
            );
    }
}
