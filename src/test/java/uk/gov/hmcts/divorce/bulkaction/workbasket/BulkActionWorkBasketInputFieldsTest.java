package uk.gov.hmcts.divorce.bulkaction.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class BulkActionWorkBasketInputFieldsTest {
    private BulkActionWorkBasketInputFields workBasketInputFields;

    @BeforeEach
    void setUp() {
        workBasketInputFields = new BulkActionWorkBasketInputFields();
    }

    @Test
    void shouldSetWorkBasketInputFields() {
        final Set<BulkActionState> stateSet = Set.of(BulkActionState.class.getEnumConstants());
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder =
            new ConfigBuilderImpl<>(BulkActionCaseData.class, stateSet);

        workBasketInputFields.configure(configBuilder);

        var workBasketBuilder = configBuilder.workBasketInputFields.get(0);
        var workBasket = workBasketBuilder.build();

        assertThat(workBasket.getFields())
            .extracting("id",
                "label",
                "listElementCode",
                "showCondition")
            .contains(
                tuple("[CASE_REFERENCE]",
                    "CCD Case Number",
                    null,
                    null)
            );
    }
}
