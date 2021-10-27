package uk.gov.hmcts.divorce.bulkaction.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class BulkActionCaseDataTest {

    @Test
    void shouldReturnBulkListCaseDetailsWhenErroredListIsEmpty() {
        BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .build();

        List<ListValue<BulkListCaseDetails>> result =
            bulkActionCaseData.calculateProcessedCases(emptyList());

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnProcessedCases() {
        BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("2")))
            .build();

        List<ListValue<BulkListCaseDetails>> result =
            bulkActionCaseData.calculateProcessedCases(List.of(getBulkListCaseDetailsListValue("1")));

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenErroredCaseIsOnlyCaseInBulkListCaseDetails() {
        BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .build();

        List<ListValue<BulkListCaseDetails>> result =
            bulkActionCaseData.calculateProcessedCases(List.of(getBulkListCaseDetailsListValue("1")));

        assertThat(result).hasSize(0);
    }
}
