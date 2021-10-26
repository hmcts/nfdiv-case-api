package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class BulkActionUtil {

    public <T> List<ListValue<T>> removeFromList(final List<ListValue<T>> targetListValues,
                                                 final List<T> removeListValues) {

        return targetListValues
            .stream()
            .filter(listValue -> !removeListValues.contains(listValue.getValue()))
            .collect(toList());
    }

    public <T> List<T> fromListValueToList(final List<ListValue<T>> targetList) {
        return targetList.stream()
            .map(ListValue::getValue)
            .collect(toList());
    }

    public <T> List<ListValue<T>> fromListToListValue(final List<T> targetList) {
        final AtomicInteger counter = new AtomicInteger(1);
        return targetList.stream()
            .map(i ->
                ListValue.<T>builder()
                    .id(String.valueOf(counter.getAndIncrement()))
                    .value(i)
                    .build()
            )
            .collect(toList());
    }

    public List<ListValue<BulkListCaseDetails>> filterProcessedCases(final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases,
                                                                      final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                                                      final Long bulkCaseId) {
        List<String> unprocessedCaseIds = unprocessedBulkCases
            .stream()
            .map(lv -> lv.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        if (isEmpty(unprocessedCaseIds)) {
            log.info("No unprocessed cases in bulk list for case id {} ", bulkCaseId);
            return bulkListCaseDetails;
        }

        return bulkListCaseDetails
            .stream()
            .filter(lv -> !unprocessedCaseIds.contains(lv.getValue().getCaseReference().getCaseReference()))
            .collect(toList());
    }
}
