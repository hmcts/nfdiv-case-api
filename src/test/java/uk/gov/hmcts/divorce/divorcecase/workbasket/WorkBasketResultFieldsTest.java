package uk.gov.hmcts.divorce.divorcecase.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class WorkBasketResultFieldsTest {

    private WorkBasketResultFields workBasketResultFields;

    @BeforeEach
    void setUp() {
        workBasketResultFields = new WorkBasketResultFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        workBasketResultFields.configure(configBuilder);

        var workBasketBuilder = configBuilder.workBasketResultFields.get(0);
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
                    "Applicant's Post Code",
                    "PostCode",
                    null),
                tuple("applicant1LastName",
                    "Applicant's Last Name",
                    null,
                    null),
                tuple("applicant2LastName",
                    "Respondent's Last Name",
                    null,
                    null)
            );
    }
}
