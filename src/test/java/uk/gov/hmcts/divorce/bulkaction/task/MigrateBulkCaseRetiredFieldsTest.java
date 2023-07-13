package uk.gov.hmcts.divorce.bulkaction.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.MigrateBulkCaseRetiredFields;

@ExtendWith(MockitoExtension.class)
class MigrateBulkCaseRetiredFieldsTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MigrateBulkCaseRetiredFields migrateBulkCaseRetiredFields;

    @Test
    void shouldMigrateBulkCaseRetiredFields() {

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .retiredFields(new BulkCaseRetiredFields())
            .build();
        caseDetails.setData(bulkActionCaseData);

        var result = migrateBulkCaseRetiredFields.apply(caseDetails);

        // Latest data version = 1. Will fail if more retired fields are added as this will push data version up.
        Assertions.assertThat(result.getData().getRetiredFields().getBulkCaseDataVersion()).isEqualTo(1);
    }
}
