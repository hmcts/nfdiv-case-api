package uk.gov.hmcts.divorce.bulkscan.ccd.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.search.ExceptionRecordSearchInputFields;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createExceptionRecordConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getSearchInputFields;

public class ExceptionRecordSearchInputFieldsTest {
    private ExceptionRecordSearchInputFields searchInputFields;

    @BeforeEach
    void setUp() {
        searchInputFields = new ExceptionRecordSearchInputFields();
    }

    @Test
    void shouldSetSearchInputFields() throws Exception {
        final ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder = createExceptionRecordConfigBuilder();

        searchInputFields.configure(configBuilder);

        assertThat(getSearchInputFields(configBuilder).getFields())
            .extracting("id", "label")
            .contains(
                tuple("deliveryDate", "Delivery date"),
                tuple("openingDate", "Opening date"),
                tuple("poBox", "PO Box"),
                tuple("poBoxJurisdiction", "PO Box jurisdiction"),
                tuple("caseReference", "New case reference"),
                tuple("attachToCaseReference", "Attach to case reference"),
                tuple("journeyClassification", "Journey classification"),
                tuple("formType", "Form type"),
                tuple("containsPayments", "Contains payments")
            );
    }
}
