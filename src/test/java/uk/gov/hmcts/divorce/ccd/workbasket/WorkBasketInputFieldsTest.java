package uk.gov.hmcts.divorce.ccd.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class WorkBasketInputFieldsTest {
    private WorkBasketInputFields workBasketInputFields;

    @BeforeEach
    void setUp() {
        workBasketInputFields = new WorkBasketInputFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

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
                    null),
                tuple("applicant1HomeAddress",
                    "Postcode",
                    "PostCode",
                    null),
                tuple("applicant1LastName",
                    "Applicant 1 Last Name",
                    null,
                    null)
            );
    }
}
