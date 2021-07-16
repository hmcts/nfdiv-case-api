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
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getSearchInputFields;

public class BulkActionSearchInputFieldsTest {
    private BulkActionSearchInputFields searchInputFields;

    @BeforeEach
    void setUp() {
        searchInputFields = new BulkActionSearchInputFields();
    }

    @Test
    void shouldSetSearchInputFields() throws Exception {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        searchInputFields.configure(configBuilder);

        assertThat(getSearchInputFields(configBuilder).getFields())
            .extracting("id", "label")
            .contains(
                tuple("[CASE_REFERENCE]", "CCD Case Number")
            );
    }
}
