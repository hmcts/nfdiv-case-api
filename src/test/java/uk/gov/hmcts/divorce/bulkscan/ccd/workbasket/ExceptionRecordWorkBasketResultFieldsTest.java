package uk.gov.hmcts.divorce.bulkscan.ccd.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.workbasket.ExceptionRecordWorkBasketResultFields;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createExceptionRecordConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getWorkBasketResultFields;

public class ExceptionRecordWorkBasketResultFieldsTest {
    private ExceptionRecordWorkBasketResultFields workbasketResultFields;

    @BeforeEach
    void setUp() {
        workbasketResultFields = new ExceptionRecordWorkBasketResultFields();
    }

    @Test
    void shouldSetWorkBasketInputFields() throws Exception {
        final ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder = createExceptionRecordConfigBuilder();

        workbasketResultFields.configure(configBuilder);

        assertThat(getWorkBasketResultFields(configBuilder).getFields())
            .extracting("id", "label")
            .contains(
                tuple("[CASE_REFERENCE]", "Exception Id"),
                tuple("[CREATED_DATE]", "Exception created date"),
                tuple("deliveryDate", "Delivery date"),
                tuple("openingDate", "Opening date"),
                tuple("poBox", "PO Box"),
                tuple("caseReference", "New case reference"),
                tuple("attachToCaseReference", "Attach to case reference"),
                tuple("journeyClassification", "Journey classification"),
                tuple("formType", "Form type")
            );
    }
}
