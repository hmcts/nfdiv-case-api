package uk.gov.hmcts.divorce.bulkaction.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class BulkActionWorkBasketResultFieldsTest {
    private BulkActionWorkBasketResultFields workBasketResultFields;

    @BeforeEach
    void setUp() {
        workBasketResultFields = new BulkActionWorkBasketResultFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() {
        final Set<BulkActionState> stateSet = Set.of(BulkActionState.class.getEnumConstants());
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder =
            new ConfigBuilderImpl<>(BulkActionCaseData.class, stateSet);

        workBasketResultFields.configure(configBuilder);

        var workBasketResultBuilder = configBuilder.workBasketResultFields.get(0);
        var workBasket = workBasketResultBuilder.build();

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
