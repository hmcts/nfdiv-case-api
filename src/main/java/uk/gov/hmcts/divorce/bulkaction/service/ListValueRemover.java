package uk.gov.hmcts.divorce.bulkaction.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class ListValueRemover {

    public <T> List<ListValue<T>> removeFromList(final List<ListValue<T>> targetListValues,
                                                 final List<T> removeListValues) {

        return targetListValues
            .stream()
            .filter(listValue -> !removeListValues.contains(listValue.getValue()))
            .collect(toList());
    }
}
