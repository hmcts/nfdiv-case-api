package uk.gov.hmcts.divorce.bulkaction.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getWorkBasketInputFields;

class BulkActionWorkBasketInputFieldsTest {
    private BulkActionWorkBasketInputFields workBasketInputFields;

    @BeforeEach
    void setUp() {
        workBasketInputFields = new BulkActionWorkBasketInputFields();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldSetWorkBasketInputFields() throws Exception {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        workBasketInputFields.configure(configBuilder);

        assertThat(getWorkBasketInputFields(configBuilder).getFields())
            .extracting("id",
                "label",
                "listElementCode",
                "showCondition")
            .contains(
                tuple("[CASE_REFERENCE]",
                    "Case Number",
                    null,
                    null)
            );
    }
}
