package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class BulkActionUtilTest {

    @InjectMocks
    private BulkActionUtil bulkActionUtil;

    @Test
    void shouldRemoveListValuesThatMatchFromList() {

        final List<ListValue<BulkListCaseDetails>> bulListValues = createListValues("1", "2", "3", "4", "5");

        final BulkListCaseDetails bulkListCaseDetails1 = bulListValues.get(0).getValue();
        final BulkListCaseDetails bulkListCaseDetails2 = bulListValues.get(1).getValue();
        final BulkListCaseDetails bulkListCaseDetails3 = bulListValues.get(2).getValue();
        final BulkListCaseDetails bulkListCaseDetails4 = bulListValues.get(3).getValue();
        final BulkListCaseDetails bulkListCaseDetails5 = bulListValues.get(4).getValue();

        final List<BulkListCaseDetails> removeListValues = asList(bulkListCaseDetails2, bulkListCaseDetails4);

        final List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.removeFromList(bulListValues, removeListValues);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo(bulkListCaseDetails1);
        assertThat(result.get(1).getId()).isEqualTo("3");
        assertThat(result.get(1).getValue()).isEqualTo(bulkListCaseDetails3);
        assertThat(result.get(2).getId()).isEqualTo("5");
        assertThat(result.get(2).getValue()).isEqualTo(bulkListCaseDetails5);
    }

    @Test
    void shouldNotRemoveListValuesForEmptyList() {

        final List<ListValue<BulkListCaseDetails>> bulListValues = createListValues("1", "2", "3", "4", "5");

        final BulkListCaseDetails bulkListCaseDetails1 = bulListValues.get(0).getValue();
        final BulkListCaseDetails bulkListCaseDetails2 = bulListValues.get(1).getValue();
        final BulkListCaseDetails bulkListCaseDetails3 = bulListValues.get(2).getValue();
        final BulkListCaseDetails bulkListCaseDetails4 = bulListValues.get(3).getValue();
        final BulkListCaseDetails bulkListCaseDetails5 = bulListValues.get(4).getValue();

        final List<BulkListCaseDetails> removeListValues = singletonList(BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("6")
                .build())
            .build());

        final List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.removeFromList(bulListValues, removeListValues);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo(bulkListCaseDetails1);
        assertThat(result.get(1).getId()).isEqualTo("2");
        assertThat(result.get(1).getValue()).isEqualTo(bulkListCaseDetails2);
        assertThat(result.get(2).getId()).isEqualTo("3");
        assertThat(result.get(2).getValue()).isEqualTo(bulkListCaseDetails3);
        assertThat(result.get(3).getId()).isEqualTo("4");
        assertThat(result.get(3).getValue()).isEqualTo(bulkListCaseDetails4);
        assertThat(result.get(4).getId()).isEqualTo("5");
        assertThat(result.get(4).getValue()).isEqualTo(bulkListCaseDetails5);
    }

    @Test
    void shouldNotRemoveListValuesThatDoNotMatch() {

        final List<ListValue<BulkListCaseDetails>> bulListValues = createListValues("1", "2", "3", "4", "5");

        final BulkListCaseDetails bulkListCaseDetails1 = bulListValues.get(0).getValue();
        final BulkListCaseDetails bulkListCaseDetails2 = bulListValues.get(1).getValue();
        final BulkListCaseDetails bulkListCaseDetails3 = bulListValues.get(2).getValue();
        final BulkListCaseDetails bulkListCaseDetails4 = bulListValues.get(3).getValue();
        final BulkListCaseDetails bulkListCaseDetails5 = bulListValues.get(4).getValue();

        final List<BulkListCaseDetails> removeListValues = emptyList();

        final List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.removeFromList(bulListValues, removeListValues);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo(bulkListCaseDetails1);
        assertThat(result.get(1).getId()).isEqualTo("2");
        assertThat(result.get(1).getValue()).isEqualTo(bulkListCaseDetails2);
        assertThat(result.get(2).getId()).isEqualTo("3");
        assertThat(result.get(2).getValue()).isEqualTo(bulkListCaseDetails3);
        assertThat(result.get(3).getId()).isEqualTo("4");
        assertThat(result.get(3).getValue()).isEqualTo(bulkListCaseDetails4);
        assertThat(result.get(4).getId()).isEqualTo("5");
        assertThat(result.get(4).getValue()).isEqualTo(bulkListCaseDetails5);
    }

    @Test
    void shouldConvertListValueToList() {
        final List<ListValue<BulkListCaseDetails>> list = createListValues("1", "2", "3", "4", "5");
        final List<BulkListCaseDetails> expectedList = createList("1", "2", "3", "4", "5");

        List<BulkListCaseDetails> result = bulkActionUtil.fromListValueToList(list);

        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void shouldConvertListToListValue() {
        final List<BulkListCaseDetails> list = createList("1", "2", "3", "4", "5");
        final List<ListValue<BulkListCaseDetails>> expectedList = createListValues("1", "2", "3", "4", "5");

        List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.fromListToListValue(list);

        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(expectedList);
    }

    private List<ListValue<BulkListCaseDetails>> createListValues(final String... caseIds) {
        final AtomicInteger counter = new AtomicInteger(1);
        return stream(caseIds)
            .map(caseId -> createListValue(String.valueOf(counter.getAndIncrement()), caseId))
            .collect(toList());
    }

    private ListValue<BulkListCaseDetails> createListValue(final String id, final String caseId) {
        final ListValue<BulkListCaseDetails> listValue = new ListValue<>();
        listValue.setId(id);
        listValue.setValue(BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(caseId)
                .build())
            .build());
        return listValue;
    }

    private List<BulkListCaseDetails> createList(final String... caseIds) {
        return stream(caseIds)
            .map(caseId -> BulkListCaseDetails.builder()
                .caseReference(CaseLink.builder()
                    .caseReference(caseId)
                    .build())
                .build())
            .collect(toList());
    }

    @Test
    void shouldBulkListCaseDetailsWhenErroredListIsEmpty() {
        List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.filterProcessedCases(
            emptyList(),
            List.of(getBulkListCaseDetailsListValue("1")),
            1L
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnProcessedCases() {
        List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.filterProcessedCases(
            List.of(getBulkListCaseDetailsListValue("1")),
            List.of(getBulkListCaseDetailsListValue("2")),
            1L
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenErroredCaseIsOnlyCaseInBulkListCaseDetails() {
        List<ListValue<BulkListCaseDetails>> result = bulkActionUtil.filterProcessedCases(
            List.of(getBulkListCaseDetailsListValue("1")),
            List.of(getBulkListCaseDetailsListValue("1")),
            1L
        );

        assertThat(result).hasSize(0);
    }
}
