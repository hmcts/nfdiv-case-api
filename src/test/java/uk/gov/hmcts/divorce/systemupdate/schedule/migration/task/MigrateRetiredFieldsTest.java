package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;


@ExtendWith(MockitoExtension.class)
class MigrateRetiredFieldsTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MigrateRetiredFields migrateRetiredFields;

    @Test
    void shouldMigrateFields() {
        final CaseData caseData = TestDataHelper.caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = migrateRetiredFields.apply(caseDetails);

        Assertions.assertThat(result.getData().getDivorceOrDissolution()).isEqualTo(DIVORCE);
    }
}
