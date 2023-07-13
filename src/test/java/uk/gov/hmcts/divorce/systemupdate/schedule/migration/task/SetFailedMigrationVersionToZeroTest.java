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
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;


@ExtendWith(MockitoExtension.class)
class SetFailedMigrationVersionToZeroTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SetFailedMigrationVersionToZero setFailedMigrationVersionToZero;

    @Test
    void shouldSetVersionToZero() {
        final CaseData caseData = TestDataHelper.caseData();
        caseData.setRetiredFields(new RetiredFields());
        caseData.getRetiredFields().setDataVersion(1);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setFailedMigrationVersionToZero.apply(caseDetails);
        Assertions.assertThat(result.getData().getRetiredFields().getDataVersion()).isEqualTo(0);
    }
}
