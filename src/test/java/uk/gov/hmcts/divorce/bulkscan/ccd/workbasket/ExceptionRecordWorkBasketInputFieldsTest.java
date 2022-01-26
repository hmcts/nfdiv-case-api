package uk.gov.hmcts.divorce.bulkscan.ccd.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.workbasket.ExceptionRecordWorkBasketInputFields;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createExceptionRecordConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getWorkBasketInputFields;

public class ExceptionRecordWorkBasketInputFieldsTest {
    private ExceptionRecordWorkBasketInputFields workbasketInputFields;

    @BeforeEach
    void setUp() {
        workbasketInputFields = new ExceptionRecordWorkBasketInputFields();
    }

    @Test
    void shouldSetWorkBasketInputFields() throws Exception {
        final ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder = createExceptionRecordConfigBuilder();

        workbasketInputFields.configure(configBuilder);

        assertThat(getWorkBasketInputFields(configBuilder).getFields())
            .extracting("id", "label")
            .contains(
                tuple("formType", "Form type"),
                tuple("containsPayments", "Contains payments")
            );
    }
}
